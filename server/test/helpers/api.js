import { spawn } from 'node:child_process';
import { createServer } from 'node:net';
import { mkdtemp } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import path from 'node:path';
import { randomBytes } from 'node:crypto';
import { fileURLToPath } from 'node:url';
import assert from 'node:assert/strict';

export async function startApi(t, overrides = {}) {
  const directory = await mkdtemp(path.join(tmpdir(), 'loveall-security-test-'));
  const listener = createServer();
  await new Promise(resolve => listener.listen(0, '127.0.0.1', resolve));
  const port = listener.address().port;
  await new Promise(resolve => listener.close(resolve));
  const database = path.join(directory, 'test.db');
  const child = spawn(process.execPath, ['src/index.js'], {
    cwd: fileURLToPath(new URL('../../', import.meta.url)),
    env: { ...process.env, NODE_ENV: 'development', PORT: String(port),
      PUBLIC_SITE_URL: `http://127.0.0.1:${port}`, DATABASE_URL: '', PHOTO_STORAGE: 'local', RENDER: '', PLAY_BILLING_ENABLED: 'false', ADMOB_PUBLISHER_ID: '',
      JWT_SECRET: randomBytes(48).toString('hex'), DB_PATH: database,
      PHOTO_UPLOAD_DIR: path.join(directory, 'photos'), ...overrides }, stdio: 'ignore',
  });
  t.after(() => child.kill());
  const base = `http://127.0.0.1:${port}`;
  let ready = false;
  for (let attempt = 0; attempt < 80; attempt++) {
    try { ready = (await fetch(`${base}/api/health`)).ok; } catch { /* startup */ }
    if (ready) break;
    await new Promise(resolve => setTimeout(resolve, 50));
  }
  assert.ok(ready, 'isolated API starts');
  const request = (route, { token, body, method = 'GET' } = {}) => fetch(`${base}${route}`, {
    method, headers: { ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}) },
    ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
  });
  async function register(label, extra = {}) {
    const response = await request('/api/auth/register', { method: 'POST', body: registration(label, extra) });
    assert.equal(response.status, 201);
    return response.json();
  }
  return { base, database, request, register };
}

export const password = 'SecurityTestPassword42';
export const registration = (label, extra = {}) => ({ name: `Test ${label}`, email: `${label.toLowerCase()}@example.test`,
  password, birthdate: '2000-01-01', gender: 'non_binary', looking_for: [], terms_version: '2026-11-20', ...extra });
