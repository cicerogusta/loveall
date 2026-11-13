import { PGlite } from '@electric-sql/pglite';
import { PGLiteSocketServer } from '@electric-sql/pglite-socket';
import pg from 'pg';
import { migratePostgres, postgresAdapter } from '../../src/database/postgres.js';

export async function postgresFixture(t) {
  if (process.env.TEST_POSTGRES_URL) {
    const url = new URL(process.env.TEST_POSTGRES_URL);
    if (!['localhost', '127.0.0.1'].includes(url.hostname) || url.pathname !== '/loveall_test') {
      throw new Error('PostgreSQL tests require a local disposable loveall_test database');
    }
    const pool = new pg.Pool({ connectionString: url.toString(), ssl: false, max: 3 });
    t.after(() => pool.end());
    await pool.query('DROP SCHEMA IF EXISTS loveall CASCADE');
    await pool.query('DROP ROLE IF EXISTS anonymous_test');
    await migratePostgres(pool);
    return { pool, db: postgresAdapter(pool), connectionString: url.toString() };
  }
  const engine = await PGlite.create();
  const socket = new PGLiteSocketServer({ db: engine, port: 0, host: '127.0.0.1', maxConnections: 5 });
  await socket.start();
  const connectionString = `postgresql://postgres@${socket.getServerConn()}/postgres`;
  const pool = new pg.Pool({
    connectionString,
    ssl: false,
    max: 1,
    options: '-c search_path=loveall,pg_catalog',
  });
  t.after(async () => {
    await pool.end();
    await socket.stop();
    await engine.close();
  });
  await migratePostgres(pool);
  return { pool, db: postgresAdapter(pool), connectionString };
}
