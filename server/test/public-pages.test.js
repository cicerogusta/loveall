import { test } from 'node:test';
import assert from 'node:assert/strict';
import { startApi, password, registration } from './helpers/api.js';

test('public support pages identify the app, controller and contact without requiring a session', async t => {
  const api = await startApi(t);
  for (const route of ['/', '/privacy', '/terms', '/child-safety', '/delete-account']) {
    const response = await api.request(route);
    assert.equal(response.status, 200, route);
    assert.match(response.headers.get('content-type'), /text\/html/);
    assert.equal(response.headers.get('cache-control'), 'no-store');
    const html = await response.text();
    assert.ok(html.includes('LoveAll'));
    assert.ok(html.includes('Cicero Pinheiro'));
    assert.ok(html.includes('loveallappcp@gmail.com'));
    assert.ok(!html.includes('SUPABASE_SERVICE_ROLE_KEY'));
  }
});

test('web deletion rejects cross-site submission, wrong credentials and missing confirmation; confirmed deletion needs no app', async t => {
  const api = await startApi(t);
  const account = await api.register('WebDelete');
  const submit = (body, origin = api.base) => fetch(`${api.base}/delete-account`, {
    method: 'POST', headers: { Origin: origin, 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams(body),
  });
  const form = { email: account.user.email, password, confirmation: 'delete' };
  assert.equal((await submit(form, 'https://attacker.example.test')).status, 403);
  assert.equal((await submit({ ...form, password: 'WrongPassword42' })).status, 401);
  assert.equal((await submit({ ...form, confirmation: '' })).status, 400);
  assert.equal((await api.request('/api/auth/me', { token: account.token })).status, 200);
  const deleted = await submit(form);
  assert.equal(deleted.status, 200);
  const html = await deleted.text();
  assert.ok(html.includes('Conta excluída'));
  assert.ok(!html.includes(password));
  assert.equal((await api.request('/api/auth/me', { token: account.token })).status, 401);
});


test('registration rejects bcrypt-truncated UTF-8 passwords and accepts the exact ASCII boundary', async t => {
  const api = await startApi(t);
  const rejected = await api.request('/api/auth/register', { method: 'POST', body: registration('Unicode', { password: 'Abc1' + 'é'.repeat(35) }) });
  assert.equal(rejected.status, 400);
  const accepted = await api.request('/api/auth/register', { method: 'POST', body: registration('Boundary', { password: 'x'.repeat(71) + '1' }) });
  assert.equal(accepted.status, 201);
});
