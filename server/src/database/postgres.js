import { AsyncLocalStorage } from 'node:async_hooks';
import { readFile } from 'node:fs/promises';
import pg from 'pg';
import { postgresSql } from './sql.js';

pg.types.setTypeParser(20, (value) => (Number.isSafeInteger(Number(value)) ? Number(value) : value));

export async function postgresOptions(env) {
  const url = new URL(env.DATABASE_URL);
  if (!['postgres:', 'postgresql:'].includes(url.protocol)) throw new Error('Invalid DATABASE_URL');
  const loopback = ['localhost', '127.0.0.1', '[::1]'].includes(url.hostname);
  // Parâmetros SSL da URL podem substituir o objeto ssl do pg: aplicamos uma
  // política única, validando certificado e hostname em toda conexão remota.
  url.search = '';
  const ssl =
    loopback && env.PG_LOCAL_NO_TLS === 'true'
      ? false
      : {
          rejectUnauthorized: true,
          ...(env.PG_CA_CERT_PATH ? { ca: await readFile(env.PG_CA_CERT_PATH, 'utf8') } : {}),
        };
  return {
    connectionString: url.toString(),
    ssl,
    max: 3,
    connectionTimeoutMillis: 15_000,
    idleTimeoutMillis: 30_000,
    statement_timeout: 20_000,
  };
}

export async function migratePostgres(pool) {
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    await client.query('CREATE SCHEMA IF NOT EXISTS loveall');
    await client.query('REVOKE ALL ON SCHEMA loveall FROM PUBLIC');
    await client.query('SET LOCAL search_path TO loveall, pg_catalog');
    await client.query('CREATE TABLE IF NOT EXISTS schema_migrations (version INTEGER PRIMARY KEY)');
    await client.query('LOCK TABLE schema_migrations IN EXCLUSIVE MODE');
    const { rows } = await client.query('SELECT version FROM schema_migrations WHERE version = 1');
    if (!rows.length) {
      await client.query(await readFile(new URL('./migrations/001-initial.sql', import.meta.url), 'utf8'));
      await client.query('INSERT INTO schema_migrations(version) VALUES (1)');
    }
    await client.query('COMMIT');
  } catch (error) {
    await client.query('ROLLBACK');
    throw error;
  } finally {
    client.release();
  }
}

export function postgresAdapter(pool) {
  const context = new AsyncLocalStorage();
  const query = async (sql, values) => {
    const transaction = context.getStore();
    if (transaction) return transaction.query(postgresSql(sql), values);
    const client = await pool.connect();
    try {
      await client.query('SET search_path TO loveall, pg_catalog');
      return await client.query(postgresSql(sql), values);
    } finally {
      client.release();
    }
  };
  return {
    dialect: 'postgres',
    prepare(sql) {
      return {
        get: async (...values) => (await query(sql, values)).rows[0],
        all: async (...values) => (await query(sql, values)).rows,
        run: async (...values) => ({ changes: (await query(sql, values)).rowCount }),
      };
    },
    transaction(work) {
      return async (...values) => {
        if (context.getStore()) throw new Error('Nested transactions are not supported');
        const client = await pool.connect();
        try {
          await client.query('BEGIN');
          await client.query('SET LOCAL search_path TO loveall, pg_catalog');
          const result = await context.run(client, () => work(...values));
          await client.query('COMMIT');
          return result;
        } catch (error) {
          await client.query('ROLLBACK');
          throw error;
        } finally {
          client.release();
        }
      };
    },
    close: () => pool.end(),
  };
}

export async function openPostgres(env = process.env) {
  const pool = new pg.Pool(await postgresOptions(env));
  pool.on('error', () => console.error('[Database] Conexão PostgreSQL indisponível.'));
  try {
    await migratePostgres(pool);
    return postgresAdapter(pool);
  } catch {
    await pool.end();
    throw new Error('Não foi possível inicializar PostgreSQL; confira a conexão e o certificado.');
  }
}
