import { test } from 'node:test';
import assert from 'node:assert/strict';
import Database from 'better-sqlite3';
import sharp from 'sharp';
import { randomUUID } from 'node:crypto';
import { sqliteAdapter } from '../src/database/sqlite-adapter.js';
import { startApi } from './helpers/api.js';

test('local async transactions isolate other requests and release the queue after rollback', async (t) => {
  const raw = new Database(':memory:');
  t.after(() => raw.close());
  raw.exec('CREATE TABLE events (id TEXT PRIMARY KEY)');
  const db = sqliteAdapter(raw);
  let release, entered;
  const hold = new Promise((resolve) => {
    release = resolve;
  });
  const started = new Promise((resolve) => {
    entered = resolve;
  });
  const transaction = db.transaction(async () => {
    await db.prepare('INSERT INTO events (id) VALUES (?)').run('uncommitted');
    entered();
    await hold;
    throw new Error('simulated failure');
  })();
  const rejected = assert.rejects(transaction, /simulated failure/);
  await started;
  let observed = false;
  const observer = db
    .prepare('SELECT COUNT(*) AS count FROM events')
    .get()
    .then((row) => {
      observed = true;
      return row.count;
    });
  await new Promise((resolve) => setImmediate(resolve));
  assert.equal(observed, false);
  release();
  await rejected;
  assert.equal(await observer, 0);
  await db.prepare('INSERT INTO events (id) VALUES (?)').run('committed');
  assert.equal((await db.prepare('SELECT COUNT(*) AS count FROM events').get()).count, 1);
});

test('concurrent likes create one match and concurrent uploads respect the reserved quota', async (t) => {
  const api = await startApi(t);
  const alice = await api.register('Alice'),
    bob = await api.register('Bob');
  const likes = await Promise.all([
    api.request('/api/likes/like', { token: alice.token, method: 'POST', body: { to_user: bob.user.id } }),
    api.request('/api/likes/like', { token: bob.token, method: 'POST', body: { to_user: alice.user.id } }),
  ]);
  assert.deepEqual(
    likes.map((response) => response.status),
    [200, 200]
  );
  const raw = new Database(api.database);
  t.after(() => raw.close());
  assert.equal(raw.prepare('SELECT COUNT(*) AS count FROM matches').get().count, 1);
  for (let index = 0; index < 11; index++)
    raw
      .prepare('INSERT INTO uploaded_photos(reference,user_id) VALUES (?,?)')
      .run(`/api/photos/${randomUUID()}.jpg`, alice.user.id);
  const image = await sharp({ create: { width: 4, height: 4, channels: 3, background: 'blue' } })
    .jpeg()
    .toBuffer();
  const send = () => {
    const body = new FormData();
    body.append('photo', new Blob([image], { type: 'image/jpeg' }), 'photo.jpg');
    return fetch(`${api.base}/api/profiles/me/photos`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${alice.token}` },
      body,
    });
  };
  const photos = await Promise.all([send(), send()]);
  assert.deepEqual(photos.map((response) => response.status).sort(), [201, 409]);
  assert.equal(
    raw.prepare('SELECT COUNT(*) AS count FROM uploaded_photos WHERE user_id = ?').get(alice.user.id).count,
    12
  );
});
