import { test } from 'node:test';
import assert from 'node:assert/strict';
import express from 'express';
import { createAppAdsRouter } from '../src/routes/app-ads.js';

async function start(t, publisher) {
  const app = express();
  app.use(createAppAdsRouter(publisher));
  const server = await new Promise(resolve => {
    const listener = app.listen(0, '127.0.0.1', () => resolve(listener));
  });
  t.after(() => new Promise(resolve => server.close(resolve)));
  return `http://127.0.0.1:${server.address().port}`;
}

test('app-ads.txt is public plain text at the domain root with the configured seller only', async t => {
  const base = await start(t, 'pub-1234567890123456');
  const response = await fetch(`${base}/app-ads.txt`);
  assert.equal(response.status, 200);
  assert.match(response.headers.get('content-type'), /^text\/plain; charset=utf-8/);
  assert.equal(await response.text(), 'google.com, pub-1234567890123456, DIRECT, f08c47fec0942fa0\n');
  assert.equal((await fetch(`${base}/api/app-ads.txt`)).status, 404);
});

test('an unconfigured publisher does not advertise a fictitious seller', async t => {
  const base = await start(t, '');
  const response = await fetch(`${base}/app-ads.txt`);
  assert.equal(response.status, 404);
  assert.ok(!(await response.text()).includes('google.com,'));
  assert.throws(() => createAppAdsRouter('pub-1234\ngoogle.com, injected'), /ADMOB_PUBLISHER_ID/);
});
