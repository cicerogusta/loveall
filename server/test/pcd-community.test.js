import { test } from 'node:test';
import assert from 'node:assert/strict';
import Database from 'better-sqlite3';
import { startApi } from './helpers/api.js';

const membership = '/api/profiles/me/pcd-community';
const discover = '/api/profiles/pcd-discover';
const like = '/api/likes/pcd-like';

test('PcD requires explicit consent, protects membership and revokes discovery on exit', async t => {
  const api = await startApi(t);
  const alice = await api.register('Alice');
  const bob = await api.register('Bob');
  const outsider = await api.register('Charlie');
  assert.equal((await api.request(membership)).status, 401);
  assert.deepEqual(await (await api.request(membership, { token: alice.token })).json(), { enabled: false });
  assert.equal((await api.request(discover, { token: alice.token })).status, 403);
  for (const body of [{ enabled: true }, { enabled: true, consent: false }, { enabled: 'true', consent: true }]) {
    assert.equal((await api.request(membership, { method: 'PUT', token: alice.token, body })).status, 400);
  }
  for (const token of [alice.token, bob.token]) {
    assert.equal((await api.request(membership, { method: 'PUT', token, body: { enabled: true, consent: true } })).status, 200);
  }
  const response = await api.request(discover, { token: alice.token });
  assert.equal(response.headers.get('cache-control'), 'no-store');
  const { profiles } = await response.json();
  assert.deepEqual(profiles.map(p => p.id), [bob.user.id]);
  for (const route of ['/api/profiles/discover', `/api/profiles/${bob.user.id}`, discover]) {
    const payload = JSON.stringify(await (await api.request(route, { token: alice.token })).json());
    assert.ok(!payload.includes('pcd_opt_in'));
    assert.ok(!payload.includes('pcd_consent'));
  }
  assert.equal((await api.request(like, { method: 'POST', token: alice.token, body: { to_user: outsider.user.id } })).status, 403);
  assert.equal((await api.request(like, { method: 'POST', token: outsider.token, body: { to_user: alice.user.id } })).status, 403);
  assert.equal((await api.request(membership, { method: 'PUT', token: bob.token, body: { enabled: false } })).status, 200);
  assert.deepEqual((await (await api.request(discover, { token: alice.token })).json()).profiles, []);
  assert.equal((await api.request(like, { method: 'POST', token: alice.token, body: { to_user: bob.user.id } })).status, 403);
  assert.equal((await api.request(discover, { token: bob.token })).status, 403);
  const db = new Database(api.database);
  t.after(() => db.close());
  const saved = db.prepare('SELECT pcd_opt_in, pcd_consent_at, pcd_consent_version FROM users WHERE id = ?').get(bob.user.id);
  assert.equal(saved.pcd_opt_in, 0);
  assert.equal(saved.pcd_consent_at, null);
  assert.equal(saved.pcd_consent_version, null);
  assert.ok(db.prepare('SELECT pcd_consent_at FROM users WHERE id = ?').get(alice.user.id).pcd_consent_at);
  db.prepare('UPDATE users SET terms_version = NULL WHERE id = ?').run(alice.user.id);
  assert.equal((await api.request(membership, { method: 'PUT', token: alice.token, body: { enabled: false } })).status, 200);
  assert.equal(db.prepare('SELECT pcd_consent_at FROM users WHERE id = ?').get(alice.user.id).pcd_consent_at, null);
});

test('discovery binds gender preferences after all relationship exclusions', async t => {
  const api = await startApi(t);
  const alice = await api.register('Alice', { looking_for: ['woman'] });
  const bob = await api.register('Bob', { gender: 'man' });
  const carol = await api.register('Carol', { gender: 'woman' });
  for (const token of [alice.token, bob.token, carol.token]) {
    await api.request(membership, { method: 'PUT', token, body: { enabled: true, consent: true } });
  }
  for (const route of [discover, '/api/profiles/discover']) {
    const { profiles } = await (await api.request(route, { token: alice.token })).json();
    assert.deepEqual(profiles.map(p => p.id), [carol.user.id]);
  }
});

test('PcD mutual likes create real matches, respect blocks and preserve connections after opt out', async t => {
  const api = await startApi(t);
  const alice = await api.register('Alice');
  const bob = await api.register('Bob');
  const charlie = await api.register('Charlie');
  for (const token of [alice.token, bob.token, charlie.token]) {
    await api.request(membership, { method: 'PUT', token, body: { enabled: true, consent: true } });
  }
  await api.request('/api/safety/block', { method: 'POST', token: alice.token, body: { blocked_id: charlie.user.id } });
  const ids = (await (await api.request(discover, { token: alice.token })).json()).profiles.map(p => p.id);
  assert.deepEqual(ids, [bob.user.id]);
  assert.equal((await api.request(like, { method: 'POST', token: charlie.token, body: { to_user: alice.user.id } })).status, 403);
  assert.equal((await api.request(like, { method: 'POST', token: alice.token, body: { to_user: bob.user.id } })).status, 200);
  const mutual = await api.request(like, { method: 'POST', token: bob.token, body: { to_user: alice.user.id } });
  const { match } = await mutual.json();
  assert.ok(match.id);
  await api.request(membership, { method: 'PUT', token: alice.token, body: { enabled: false } });
  const matches = await (await api.request('/api/matches', { token: alice.token })).json();
  assert.equal(matches.matches[0].match_id, match.id);
  assert.equal((await api.request(`/api/messages/${match.id}`, { method: 'POST', token: alice.token, body: { content: 'Olá!' } })).status, 201);
});
