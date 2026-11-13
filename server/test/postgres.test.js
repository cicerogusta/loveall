import { test } from 'node:test';
import assert from 'node:assert/strict';
import sharp from 'sharp';
import { postgresFixture } from './helpers/postgres.js';
import { startApi, password } from './helpers/api.js';
import { postgresOptions, migratePostgres } from '../src/database/postgres.js';
import { postgresSql } from '../src/database/sql.js';
import { PlayBillingService } from '../src/services/play/play-billing-service.js';
import { purchase, product, now, notification } from './helpers/play.js';
import { randomBytes } from 'node:crypto';

test('PostgreSQL TLS is mandatory remotely and connection-string flags cannot disable certificate validation', async () => {
  const remote = await postgresOptions({
    DATABASE_URL: 'postgresql://user:password@db.example.test/app?sslmode=disable',
    PG_LOCAL_NO_TLS: 'true',
  });
  assert.equal(remote.ssl.rejectUnauthorized, true);
  assert.ok(!remote.connectionString.includes('sslmode'));
  const local = await postgresOptions({
    DATABASE_URL: 'postgresql://localhost/app',
    PG_LOCAL_NO_TLS: 'true',
  });
  assert.equal(local.ssl, false);
  assert.equal(
    postgresSql("SELECT '?' AS literal, ? AS value, 'it''s ?' AS escaped"),
    "SELECT '?' AS literal, $1 AS value, 'it''s ?' AS escaped"
  );
});

test('PostgreSQL migrations are repeatable, private and preserve async transaction rollback and billing', async (t) => {
  const { pool, db } = await postgresFixture(t);
  await migratePostgres(pool);
  assert.equal((await db.prepare('SELECT COUNT(*) AS count FROM schema_migrations').get()).count, 1);
  await pool.query('CREATE ROLE anonymous_test');
  assert.equal(
    (await pool.query("SELECT has_schema_privilege('anonymous_test', 'loveall', 'USAGE') AS allowed")).rows[0]
      .allowed,
    false
  );
  const insert = db.prepare(
    "INSERT INTO users(id,email,password,name,birthdate,gender) VALUES (?, ?, 'hash', 'Test', '2000-01-01', 'man')"
  );
  await assert.rejects(
    db.transaction(async () => {
      await insert.run('rollback', 'rollback@example.test');
      await Promise.resolve();
      throw new Error('rollback fixture');
    })(),
    /rollback fixture/
  );
  assert.equal(await db.prepare('SELECT id FROM users WHERE id = ?').get('rollback'), undefined);
  await insert.run('alice', 'alice@example.test');
  const resources = new Map([['token', purchase()]]);
  const gateway = {
    async getSubscription(token) {
      return resources.get(token);
    },
    async acknowledge() {},
  };
  const service = new PlayBillingService(
    db,
    gateway,
    { productIds: [product], packageName: 'com.loveall.app', encryptionKey: randomBytes(32) },
    () => now
  );
  await service.configuration('alice');
  await service.configuration('alice');
  assert.equal((await service.verify('alice', product, 'token')).ad_free, true);
  resources.set('token', purchase({ subscriptionState: 'SUBSCRIPTION_STATE_EXPIRED' }));
  await service.notification(notification('token'));
  assert.equal((await service.entitlement('alice')).ad_free, false);
});

test('HTTP contract works with PostgreSQL: registration, discovery, match, chat, photos, blocks and deletion', async (t) => {
  const { connectionString, db } = await postgresFixture(t);
  const api = await startApi(t, { DATABASE_URL: connectionString, PG_LOCAL_NO_TLS: 'true' });
  const alice = await api.register('Alice'),
    bob = await api.register('Bob');
  const asAlice = (route, options = {}) => api.request(route, { token: alice.token, ...options });
  let response = await asAlice('/api/profiles/discover');
  assert.equal(response.status, 200);
  assert.equal((await response.json()).profiles[0].id, bob.user.id);
  assert.equal(
    (await asAlice('/api/likes/like', { method: 'POST', body: { to_user: bob.user.id } })).status,
    200
  );
  response = await api.request('/api/likes/like', {
    token: bob.token,
    method: 'POST',
    body: { to_user: alice.user.id },
  });
  const match = (await response.json()).match;
  assert.ok(match?.id);
  response = await asAlice(`/api/messages/${match.id}`, {
    method: 'POST',
    body: { content: 'Olá, tudo bem?' },
  });
  assert.equal(response.status, 201);
  response = await asAlice('/api/matches');
  assert.equal(response.status, 200);
  assert.equal((await response.json()).matches[0].last_message.content, 'Olá, tudo bem?');
  const image = await sharp({ create: { width: 20, height: 20, channels: 3, background: 'red' } })
    .jpeg()
    .toBuffer();
  const form = new FormData();
  form.append('photo', new Blob([image], { type: 'image/jpeg' }), 'photo.jpg');
  response = await fetch(`${api.base}/api/profiles/me/photos`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${alice.token}` },
    body: form,
  });
  assert.equal(response.status, 201);
  const { photo } = await response.json();
  assert.equal((await fetch(`${api.base}${photo}`)).status, 200);
  response = await asAlice('/api/profiles/me', { method: 'PUT', body: { photos: [photo], bio: 'Olá!' } });
  assert.equal(response.status, 200);
  assert.deepEqual((await response.json()).user.photos, [photo]);
  response = await asAlice('/api/safety/block', { method: 'POST', body: { blocked_id: bob.user.id } });
  assert.equal(response.status, 200);
  assert.equal((await asAlice(`/api/profiles/${bob.user.id}`)).status, 403);
  assert.equal((await asAlice('/api/auth/me', { method: 'DELETE', body: { password } })).status, 204);
  assert.equal((await fetch(`${api.base}${photo}`)).status, 404);
  assert.equal((await db.prepare('SELECT COUNT(*) AS count FROM users').get()).count, 1);
  assert.equal((await db.prepare('SELECT COUNT(*) AS count FROM messages').get()).count, 0);
});
