import { test } from 'node:test';
import assert from 'node:assert/strict';
import { createClient } from '@supabase/supabase-js';
import { createPhotoStore, photoFilename, supabasePhotoStore } from '../src/services/photo-store.js';

test('Supabase adapter sends JPEG privately and propagates upload/deletion failures for retry', async () => {
  const requests = [],
    objects = new Map();
  let failRemove = true,
    failUpload = false;
  const client = createClient('https://storage.example.test', 'test-server-only-key', {
    auth: { persistSession: false, autoRefreshToken: false },
    global: {
      fetch: async (url, options) => {
        const method = options.method,
          pathname = new URL(url).pathname;
        requests.push({ method, pathname });
        if (method === 'POST' && pathname.includes('/object/')) {
          if (failUpload)
            return Response.json(
              { statusCode: '503', error: 'Unavailable', message: 'retry later' },
              { status: 503 }
            );
          objects.set('photo', new Uint8Array([1, 2, 3]));
          return Response.json({ Key: 'photo' });
        }
        if (method === 'GET')
          return new Response(objects.get('photo'), { headers: { 'Content-Type': 'image/jpeg' } });
        if (method === 'DELETE') {
          if (failRemove)
            return Response.json(
              { statusCode: '503', error: 'Unavailable', message: 'retry later' },
              { status: 503 }
            );
          objects.delete('photo');
          return Response.json([]);
        }
        throw new Error('Unexpected request');
      },
    },
  });
  const store = supabasePhotoStore(client, 'loveall-photos');
  await store.put('photo.jpg', Buffer.from([1, 2, 3]));
  assert.deepEqual(await store.get('photo.jpg'), Buffer.from([1, 2, 3]));
  await assert.rejects(store.remove('photo.jpg'), /deletion unavailable/);
  assert.equal(objects.size, 1);
  failRemove = false;
  await store.remove('photo.jpg');
  assert.equal(objects.size, 0);
  failUpload = true;
  await assert.rejects(store.put('photo.jpg', Buffer.from([1])), /upload unavailable/);
  assert.ok(requests.every((r) => !r.pathname.includes('/public/')));
});

test('ephemeral Render storage and public photo buckets are refused at startup', async (t) => {
  await assert.rejects(createPhotoStore({ RENDER: 'true' }), /persistent photo storage/);
  await assert.rejects(createPhotoStore({ PHOTO_STORAGE: 'invalid' }), /Invalid PHOTO_STORAGE/);
  await assert.rejects(createPhotoStore({ PHOTO_STORAGE: 'supabase' }), /Configure PostgreSQL/);
  assert.equal(photoFilename('/api/photos/../../private'), undefined);
  t.mock.method(globalThis, 'fetch', async () => Response.json({ name: 'loveall-photos', public: true }));
  await assert.rejects(
    createPhotoStore({
      PHOTO_STORAGE: 'supabase',
      DATABASE_URL: 'postgresql://localhost/test',
      SUPABASE_URL: 'https://storage.example.test',
      SUPABASE_SERVICE_ROLE_KEY: 'test-server-only-key',
      SUPABASE_PHOTO_BUCKET: 'loveall-photos',
    }),
    /private Supabase photo bucket/
  );
});
