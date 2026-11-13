import { AsyncLocalStorage } from 'node:async_hooks';

// A fila impede intercalar requisições durante uma transação assíncrona local.
export function sqliteAdapter(connection) {
  const context = new AsyncLocalStorage();
  let tail = Promise.resolve();
  const enqueue = (work) => {
    if (context.getStore()) return Promise.resolve().then(work);
    const result = tail.then(work);
    tail = result.catch(() => {});
    return result;
  };
  return {
    dialect: 'sqlite',
    prepare(sql) {
      const statement = connection.prepare(sql.replace(/\s+FOR UPDATE\b/gi, ''));
      return Object.fromEntries(
        ['get', 'all', 'run'].map((method) => [
          method,
          (...values) => enqueue(() => statement[method](...values)),
        ])
      );
    },
    transaction(work) {
      return (...values) =>
        enqueue(async () => {
          if (context.getStore()) throw new Error('Nested transactions are not supported');
          connection.exec('BEGIN IMMEDIATE');
          try {
            const result = await context.run(true, () => work(...values));
            connection.exec('COMMIT');
            return result;
          } catch (error) {
            connection.exec('ROLLBACK');
            throw error;
          }
        });
    },
    close: () => enqueue(() => connection.close()),
  };
}
