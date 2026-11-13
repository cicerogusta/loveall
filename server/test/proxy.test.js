import { test } from 'node:test';
import assert from 'node:assert/strict';
import express from 'express';
import { proxyHops } from '../src/config/proxy.js';

test('direct access ignores forwarded addresses and one proxy uses the closest forwarded client', async t => {
  for (const hops of [undefined, '1']) {
    const app = express();
    app.set('trust proxy', proxyHops(hops));
    app.get('/', (req, res) => res.json({ ip: req.ip }));
    const server = app.listen(0, '127.0.0.1');
    await new Promise(resolve => server.once('listening', resolve));
    t.after(() => server.close());
    const response = await fetch(`http://127.0.0.1:${server.address().port}`, {
      headers: { 'X-Forwarded-For': '192.0.2.123, 198.51.100.42' },
    });
    assert.equal((await response.json()).ip, hops === '1' ? '198.51.100.42' : '127.0.0.1');
  }
});

test('unrestricted or malformed proxy trust configuration is rejected', () => {
  for (const value of ['true', '2', '-1', '1abc', '']) assert.throws(() => proxyHops(value));
});
