import { createClient } from '@supabase/supabase-js';
import fs from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

export const photoFilename = (reference) => /^\/api\/photos\/([0-9a-f-]{36}\.jpg)$/.exec(reference)?.[1];
export function localPhotoStore(directory) {
  const ready = fs.mkdir(directory, { recursive: true });
  return {
    kind: 'local',
    directory,
    async put(name, bytes) {
      await ready;
      await fs.writeFile(path.join(directory, name), bytes, { flag: 'wx' });
    },
    async get(name) {
      try {
        return await fs.readFile(path.join(directory, name));
      } catch (e) {
        if (e.code === 'ENOENT') return null;
        throw e;
      }
    },
    async remove(name) {
      await fs.rm(path.join(directory, name), { force: true });
    },
  };
}
export function supabasePhotoStore(client, bucket) {
  const storage = client.storage.from(bucket);
  return {
    kind: 'supabase',
    async put(name, bytes) {
      const { error } = await storage.upload(name, bytes, { contentType: 'image/jpeg', upsert: false });
      if (error) throw new Error('Photo upload unavailable');
    },
    async get(name) {
      const { data, error } = await storage.download(name);
      if (error) {
        if (String(error.statusCode) === '404') return null;
        throw new Error('Photo download unavailable');
      }
      return Buffer.from(await data.arrayBuffer());
    },
    async remove(name) {
      const { error } = await storage.remove([name]);
      if (error) throw new Error('Photo deletion unavailable');
    },
  };
}
export async function createPhotoStore(env = process.env) {
  const remote = env.PHOTO_STORAGE === 'supabase';
  if (env.PHOTO_STORAGE && !['local', 'supabase'].includes(env.PHOTO_STORAGE))
    throw new Error('Invalid PHOTO_STORAGE');
  if (env.RENDER === 'true' && (!env.DATABASE_URL || !remote))
    throw new Error('Render requires PostgreSQL and persistent photo storage');
  if (remote) {
    if (
      !env.DATABASE_URL ||
      !env.SUPABASE_URL ||
      !env.SUPABASE_SERVICE_ROLE_KEY ||
      !env.SUPABASE_PHOTO_BUCKET
    ) {
      throw new Error('Configure PostgreSQL and Supabase private storage');
    }
    const url = new URL(env.SUPABASE_URL);
    if (url.protocol !== 'https:' || url.username || url.password) throw new Error('Supabase requires HTTPS');
    if (!/^[a-z0-9-]{1,63}$/.test(env.SUPABASE_PHOTO_BUCKET)) throw new Error('Invalid bucket name');
    const client = createClient(url.origin, env.SUPABASE_SERVICE_ROLE_KEY, {
      auth: { persistSession: false, autoRefreshToken: false, detectSessionInUrl: false },
      global: { fetch: (input, init) => fetch(input, { ...init, signal: AbortSignal.timeout(30_000) }) },
    });
    const { data, error } = await client.storage.getBucket(env.SUPABASE_PHOTO_BUCKET);
    if (error || !data || data.public)
      throw new Error('Create a private Supabase photo bucket before starting the API');
    return supabasePhotoStore(client, env.SUPABASE_PHOTO_BUCKET);
  }
  const defaultDir = fileURLToPath(new URL('../../data/', import.meta.url));
  return localPhotoStore(
    path.resolve(
      env.PHOTO_UPLOAD_DIR ||
        path.join(env.DB_PATH ? path.dirname(path.resolve(env.DB_PATH)) : defaultDir, 'photos')
    )
  );
}
