import fs from 'node:fs/promises';
import path from 'node:path';
import db from '../database.js';
import { createPhotoStore, photoFilename } from './photo-store.js';

export const photoStore = await createPhotoStore();
export async function markRemovedPhotos(userId, retained) {
  for (const row of await db.prepare('SELECT reference FROM uploaded_photos WHERE user_id = ?').all(userId)) {
    if (!retained.includes(row.reference))
      await db
        .prepare('UPDATE uploaded_photos SET pending_delete = 1 WHERE reference = ?')
        .run(row.reference);
  }
}
export async function purgeDeletedPhotos() {
  // Um upload em andamento termina antes da remoção. Reservas abandonadas expiram.
  const rows = await db
    .prepare(
      "SELECT reference FROM uploaded_photos WHERE pending_delete = 1 AND (ready = 1 OR created_at < datetime('now', '-1 day'))"
    )
    .all();
  for (const { reference } of rows) {
    const name = photoFilename(reference);
    if (!name) continue;
    try {
      await photoStore.remove(name);
      await db
        .prepare('DELETE FROM uploaded_photos WHERE reference = ? AND pending_delete = 1')
        .run(reference);
    } catch {
      console.error('[Photos] Remoção pendente; nova tentativa será feita na manutenção.');
    }
  }
}
let maintenance;
export function maintainPhotos() {
  if (maintenance) return maintenance;
  maintenance = cleanup().finally(() => {
    maintenance = null;
  });
  return maintenance;
}
async function cleanup() {
  await db
    .prepare(
      "UPDATE uploaded_photos SET pending_delete = 1 WHERE attached = 0 AND created_at < datetime('now', '-1 day')"
    )
    .run();
  await purgeDeletedPhotos();
  if (photoStore.kind !== 'local') return;
  await fs.mkdir(photoStore.directory, { recursive: true });
  for (const entry of await fs.readdir(photoStore.directory, { withFileTypes: true })) {
    if (!entry.isFile() || !photoFilename(`/api/photos/${entry.name}`)) continue;
    if (
      await db.prepare('SELECT 1 FROM uploaded_photos WHERE reference = ?').get(`/api/photos/${entry.name}`)
    )
      continue;
    const filename = path.join(photoStore.directory, entry.name);
    try {
      if (Date.now() - (await fs.stat(filename)).mtimeMs > 86400000) await fs.rm(filename);
    } catch {
      console.error('[Photos] Arquivo órfão pendente de remoção.');
    }
  }
}
