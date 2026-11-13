import 'dotenv/config';

// A nuvem usa PostgreSQL; sem DATABASE_URL, o banco local existente é preservado.
const db = process.env.DATABASE_URL
  ? await (await import('./database/postgres.js')).openPostgres()
  : (await import('./database/sqlite-adapter.js')).sqliteAdapter(
      (await import('./database-sqlite.js')).default
    );
export default db;
