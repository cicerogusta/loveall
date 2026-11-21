import { test } from 'node:test';
import assert from 'node:assert/strict';
import { spawn } from 'node:child_process';
import { createServer } from 'node:net';
import { mkdtemp, readFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import sharp from 'sharp';
import { randomBytes } from 'node:crypto';

test('authenticated photo upload, validation, serving and profile persistence', { timeout: 20000 }, async (t) => {
  const directory = await mkdtemp(path.join(tmpdir(), 'loveall-photo-test-'));
  const listener = createServer();
  await new Promise(resolve => listener.listen(0, '127.0.0.1', resolve));
  const port = listener.address().port;
  await new Promise(resolve => listener.close(resolve));
  const child = spawn(process.execPath, ['src/index.js'], {
    cwd: fileURLToPath(new URL('../', import.meta.url)),
    env: { ...process.env, NODE_ENV: 'development', PORT: String(port),
      JWT_SECRET: randomBytes(32).toString('hex'), DB_PATH: path.join(directory, 'test.db'),
      PHOTO_UPLOAD_DIR: path.join(directory, 'photos') }, stdio: 'ignore',
  });
  t.after(() => child.kill());
  const base = `http://127.0.0.1:${port}`;
  let ready = false;
  for (let i = 0; i < 80; i++) {
    try { ready = (await fetch(`${base}/api/health`)).ok; } catch { /* wait for startup */ }
    if (ready) break;
    await new Promise(resolve => setTimeout(resolve, 100));
  }
  assert.ok(ready, 'isolated API should start');
  assert.equal((await fetch(`${base}/api/profiles/me/photos`, { method: 'POST' })).status, 401);
  const registration = await fetch(`${base}/api/auth/register`, { method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name: 'Photo Test', email: 'photo@example.test', password: 'TestPassword42', birthdate: '2000-01-01', gender: 'non_binary', terms_version: '2026-11-21' }) });
  assert.equal(registration.status, 201);
  const { token } = await registration.json();
  const headers = { Authorization: `Bearer ${token}` };
  async function upload(bytes, mime = 'image/jpeg') {
    const form = new FormData();
    form.append('photo', new Blob([bytes], { type: mime }), 'photo.jpg');
    return fetch(`${base}/api/profiles/me/photos`, { method: 'POST', headers, body: form });
  }
  assert.equal((await upload('not a photo')).status, 400);
  assert.equal((await upload(Buffer.alloc(4 * 1024 * 1024 + 1))).status, 413);
  const image = await readFile(new URL('./fixtures/profile.jpg', import.meta.url));
  assert.equal((await upload(image, 'text/html')).status, 400);
  const response = await upload(image);
  assert.equal(response.status, 201);
  const { photo } = await response.json();
  assert.match(photo, /^\/api\/photos\/[0-9a-f-]{36}\.jpg$/);
  const served = await fetch(`${base}${photo}`);
  assert.equal(served.headers.get('content-type'), 'image/jpeg');
  assert.equal(served.headers.get('x-content-type-options'), 'nosniff');
  const normalized = Buffer.from(await served.arrayBuffer());
  const metadata = await sharp(normalized).metadata();
  assert.equal(metadata.format, 'jpeg');
  assert.equal(metadata.exif, undefined);
  assert.ok(metadata.width <= 1600 && metadata.height <= 1600);
  const saved = await fetch(`${base}/api/profiles/me`, { method: 'PUT',
    headers: { ...headers, 'Content-Type': 'application/json' }, body: JSON.stringify({ photos: [photo] }) });
  assert.equal(saved.status, 200);
  const current = await fetch(`${base}/api/auth/me`, { headers });
  assert.deepEqual((await current.json()).user.photos, [photo]);
});
