import { test } from 'node:test';
import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { readFile } from 'node:fs/promises';
import jwt from 'jsonwebtoken';
import Database from 'better-sqlite3';
import { startApi, registration, password } from './helpers/api.js';

test('startup rejects missing, short and known-default JWT secrets', () => {
  for (const secret of ['', 'short', 'loveall_secret', 'MUDE_ESTA_CHAVE_EM_PRODUCAO']) {
    const result = spawnSync(process.execPath, ['--input-type=module', '-e', "import './src/config/security.js'"],
      { env: { ...process.env, JWT_SECRET: secret }, encoding: 'utf8', timeout: 5000 });
    assert.notEqual(result.status, 0);
    assert.match(result.stderr, /JWT_SECRET/);
  }
});

test('registration validates real dates, payload types, identity and explicit consent', async t => {
  const api = await startApi(t);
  for (const extra of [{ birthdate: '2000-02-31' }, { birthdate: '2015-01-01' }, { terms_version: null },
    { gender: 'invented' }, { looking_for: ['invented'] }, { email: {} }, { password: {} }, { name: [] }]) {
    assert.equal((await api.request('/api/auth/register', { method: 'POST', body: registration('Invalid', extra) })).status, 400);
  }
  const account = await api.register('Valid');
  const forged = jwt.sign({ id: account.user.id }, 'loveall_secret');
  assert.equal((await api.request('/api/auth/me', { token: forged })).status, 401);
});

test('profile rejects invalid values and another account photo without partial writes', async t => {
  const api = await startApi(t);
  const alice = await api.register('Alice');
  const bob = await api.register('Bob');
  const photo = await upload(api, bob.token);
  for (const body of [{ name: 'X' }, { age_min: 1 }, { age_min: 50, age_max: 20 }, { max_distance: 10000 },
    { bio: 'x'.repeat(501) }, { looking_for: {} }, { photos: [photo] }, { photos: ['https://tracker.invalid/image.jpg'] },
    { location_lat: 91 }, { is_premium: 1 }, { gender: 'invented' }, JSON.parse('{"__proto__":{}}'), { constructor: {} }]) {
    assert.equal((await api.request('/api/profiles/me', { method: 'PUT', token: alice.token, body })).status, 400);
  }
  const current = await api.request('/api/auth/me', { token: alice.token });
  assert.equal((await current.json()).user.name, 'Test Alice');
  assert.equal((await api.request('/api/profiles/me', { method: 'PUT', token: bob.token, body: { photos: [photo], bio: 'Hello' } })).status, 200);
  assert.equal((await api.request('/api/profiles/me', { method: 'PUT', token: bob.token, body: { photos: [] } })).status, 200);
  assert.equal((await fetch(`${api.base}${photo}`)).status, 404);
});

test('legacy accounts must accept current terms before posting, but can delete without accepting', async t => {
  const api = await startApi(t);
  const account = await api.register('Legacy');
  const db = new Database(api.database); t.after(() => db.close());
  db.prepare('UPDATE users SET terms_version = NULL WHERE id = ?').run(account.user.id);
  assert.equal((await api.request('/api/profiles/me', { method: 'PUT', token: account.token, body: { bio: 'Hello' } })).status, 403);
  assert.equal((await api.request('/api/auth/terms', { method: 'POST', token: account.token, body: { terms_version: 'obsolete' } })).status, 400);
  assert.equal((await api.request('/api/auth/terms', { method: 'POST', token: account.token, body: { terms_version: '2026-11-20' } })).status, 200);
  assert.equal((await api.request('/api/profiles/me', { method: 'PUT', token: account.token, body: { bio: 'Hello' } })).status, 200);
  db.prepare('UPDATE users SET terms_version = NULL WHERE id = ?').run(account.user.id);
  assert.equal((await api.request('/api/auth/me', { method: 'DELETE', token: account.token, body: { password } })).status, 204);
});

test('unavailable payments, calls and sockets cannot mutate account balances', async t => {
  const api = await startApi(t);
  const account = await api.register('Buyer');
  for (const route of ['subscribe', 'check', 'log', 'mercado-pago/webhook']) {
    assert.equal((await api.request(`/api/calls/${route}`, { method: 'POST', token: account.token,
      body: { plan: 'premium', duration: -100, type: 'payment', data: { id: 'fake', status: 'approved' } } })).status, 503);
  }
  assert.equal((await api.request('/api/calls/plans')).status, 200);
  assert.deepEqual((await (await api.request('/api/calls/plans')).json()).plans, {});
  const plan = await (await api.request('/api/calls/my-plan', { token: account.token })).json();
  assert.equal(plan.subscription.plan, 'free');
  assert.equal(plan.subscription.call_minutes_remaining, 0);
  assert.equal((await fetch(`${api.base}/socket.io/?EIO=4&transport=polling`)).status, 404);
});

test('photos disappear immediately when marked for removal and abandoned drafts are purged', async t => {
  const api = await startApi(t);
  const account = await api.register('Draft');
  const expired = await upload(api, account.token);
  const retained = await upload(api, account.token);
  const db = new Database(api.database); t.after(() => db.close());
  db.prepare("UPDATE uploaded_photos SET created_at = datetime('now', '-2 days') WHERE reference = ?").run(expired);
  db.prepare('UPDATE uploaded_photos SET attached = 1 WHERE reference = ?').run(retained);
  const result = spawnSync(process.execPath, ['--input-type=module', '-e',
    "import { maintainPhotos } from './src/services/photo-storage.js'; maintainPhotos();"],
  { env: { ...process.env, DB_PATH: api.database }, encoding: 'utf8', timeout: 5000 });
  assert.equal(result.status, 0, result.stderr);
  assert.equal((await fetch(`${api.base}${expired}`)).status, 404);
  const response = await fetch(`${api.base}${retained}`);
  assert.equal(response.status, 200);
  assert.equal(response.headers.get('cache-control'), 'no-store');
  db.prepare('UPDATE uploaded_photos SET pending_delete = 1 WHERE reference = ?').run(retained);
  assert.equal((await fetch(`${api.base}${retained}`)).status, 404);
});

test('blocked profiles and malformed interactions are rejected without exposing birthdates', async t => {
  const api = await startApi(t);
  const a = await api.register('Viewer'), b = await api.register('Partner');
  const publicProfile = await (await api.request(`/api/profiles/${b.user.id}`, { token: a.token })).json();
  assert.equal(publicProfile.profile.birthdate, undefined);
  assert.equal(publicProfile.profile.email, undefined);
  for (const [route, body] of [['/api/likes/like', { to_user: {} }], ['/api/likes/dislike', { to_user: {} }],
    ['/api/safety/report', { reported_id: {}, reason: 'spam' }], ['/api/safety/block', { blocked_id: {} }]]) {
    assert.equal((await api.request(route, { method: 'POST', token: a.token, body })).status, 400);
  }
  assert.equal((await api.request('/api/safety/block', { method: 'POST', token: a.token, body: { blocked_id: b.user.id } })).status, 200);
  assert.equal((await api.request(`/api/profiles/${b.user.id}`, { token: a.token })).status, 403);
  assert.equal((await api.request(`/api/profiles/${a.user.id}`, { token: b.token })).status, 403);
});

test('deletion requires password and removes related records and photos while preserving unrelated conversations', async t => {
  const api = await startApi(t);
  const a = await api.register('Alpha'), b = await api.register('Beta'), c = await api.register('Gamma');
  const photo = await upload(api, a.token);
  await api.request('/api/profiles/me', { method: 'PUT', token: a.token, body: { photos: [photo] } });
  const db = new Database(api.database); t.after(() => db.close());
  const aid = a.user.id, bid = b.user.id, cid = c.user.id;
  db.prepare('INSERT INTO matches (id, user1, user2) VALUES (?, ?, ?)').run('shared', aid, bid);
  db.prepare('INSERT INTO matches (id, user1, user2) VALUES (?, ?, ?)').run('unrelated', bid, cid);
  db.prepare('INSERT INTO messages (id, match_id, sender_id, content) VALUES (?, ?, ?, ?)').run('shared-message', 'shared', bid, 'Hello');
  db.prepare('INSERT INTO messages (id, match_id, sender_id, content) VALUES (?, ?, ?, ?)').run('other-message', 'unrelated', bid, 'Keep');
  for (const [table, first, second] of [['likes','from_user','to_user'], ['dislikes','from_user','to_user'], ['blocks','blocker_id','blocked_id']]) {
    db.prepare(`INSERT INTO ${table} (id, ${first}, ${second}) VALUES (?, ?, ?)`).run(table, bid, aid);
  }
  db.prepare('INSERT INTO reports (id, reporter_id, reported_id, reason) VALUES (?, ?, ?, ?)').run('report', bid, aid, 'spam');
  db.prepare('INSERT INTO call_history (id, caller_id, receiver_id, match_id, call_type) VALUES (?, ?, ?, ?, ?)').run('call', bid, aid, 'shared', 'audio');
  db.prepare('INSERT INTO payment_logs (id, user_id, transaction_id, plan, amount) VALUES (?, ?, ?, ?, ?)').run('payment', aid, 'old', 'basic', 9.9);
  db.prepare('INSERT INTO play_accounts (user_id, obfuscated_id) VALUES (?, ?)').run(aid, 'test-obfuscated-account');
  db.prepare('INSERT INTO play_purchases (token_hash, user_id, product_id, token_cipher, state, verified_at) VALUES (?, ?, ?, ?, ?, ?)').run('token-hash', aid, 'test-product', 'test-cipher', 'SUBSCRIPTION_STATE_ACTIVE', Date.now());
  assert.equal((await api.request('/api/auth/me', { method: 'DELETE', body: { password } })).status, 401);
  assert.equal((await api.request('/api/auth/me', { method: 'DELETE', token: a.token, body: { password: 'incorrect' } })).status, 403);
  assert.ok(db.prepare('SELECT id FROM users WHERE id = ?').get(aid));
  assert.equal((await api.request('/api/auth/me', { method: 'DELETE', token: a.token, body: { password } })).status, 204);
  assert.equal((await api.request('/api/auth/me', { token: a.token })).status, 401);
  for (const table of ['likes','dislikes','blocks','reports','call_history','payment_logs','uploaded_photos','play_purchases','play_accounts']) {
    assert.equal(db.prepare(`SELECT COUNT(*) AS count FROM ${table}`).get().count, 0, table);
  }
  assert.equal(db.prepare('SELECT COUNT(*) AS count FROM users').get().count, 2);
  assert.equal(db.prepare('SELECT COUNT(*) AS count FROM messages').get().count, 1);
  assert.ok(db.prepare('SELECT id FROM messages WHERE id = ?').get('other-message'));
  assert.equal((await fetch(`${api.base}${photo}`)).status, 404);
});

async function upload(api, token) {
  const form = new FormData();
  const bytes = await readFile(new URL('./fixtures/profile.jpg', import.meta.url));
  form.append('photo', new Blob([bytes], { type: 'image/jpeg' }), 'photo.jpg');
  const response = await fetch(`${api.base}/api/profiles/me/photos`, { method: 'POST', headers: { Authorization: `Bearer ${token}` }, body: form });
  assert.equal(response.status, 201);
  return (await response.json()).photo;
}
