import { test } from 'node:test';
import assert from 'node:assert/strict';
import pg from 'pg';
import { databaseStartupError } from '../src/database/startup-error.js';
import { openPostgres } from '../src/database/postgres.js';

test('startup diagnostics identify certificate, authentication and network failures without driver secrets', () => {
  for (const code of ['SELF_SIGNED_CERT_IN_CHAIN', '28P01', 'ENETUNREACH', 'ETIMEDOUT', '42501']) {
    const error = databaseStartupError(
      { code, message: 'sensitive-password', detail: 'sensitive-row' },
      'connection'
    );
    assert.equal(error.code, code);
    assert.ok(error.message.includes(`[Database:${code}]`));
    assert.ok(!String(error.stack).includes('sensitive'));
    assert.equal(error.cause, undefined);
  }
  const unknown = databaseStartupError(
    { code: 'sensitive-code', message: 'sensitive-password' },
    'connection'
  );
  assert.equal(unknown.code, 'POSTGRES_STARTUP_FAILED');
  assert.ok(!JSON.stringify(unknown).includes('sensitive'));
});

test('invalid URLs and unreplaced password placeholders fail without exposing connection credentials', async () => {
  for (const url of [
    'https://user:sensitive-password@host/db',
    'invalid-sensitive-password',
    'postgresql://postgres.project:[YOUR-PASSWORD]@pooler.example.test:5432/postgres',
  ]) {
    await assert.rejects(openPostgres({ DATABASE_URL: url }), (error) => {
      assert.equal(error.code, 'DATABASE_CONFIGURATION_INVALID');
      assert.ok(!error.stack.includes('sensitive-password'));
      assert.ok(!error.stack.includes('[YOUR-PASSWORD]'));
      return true;
    });
  }
});

test('a failed pool connection is closed and reports a safe actionable diagnostic', async (t) => {
  let closed = false;
  t.mock.method(pg.Pool.prototype, 'connect', async () => {
    throw Object.assign(new Error('postgresql://user:sensitive-password@host/db'), { code: '28P01' });
  });
  t.mock.method(pg.Pool.prototype, 'end', async () => {
    closed = true;
  });
  await assert.rejects(
    openPostgres({ DATABASE_URL: 'postgresql://user:fixture@host.example.test/postgres' }),
    (error) => {
      assert.equal(error.code, '28P01');
      assert.ok(error.message.includes('senha do banco'));
      assert.ok(!error.stack.includes('sensitive-password'));
      return true;
    }
  );
  assert.equal(closed, true);
});
