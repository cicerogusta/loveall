import db from '../database.js';
import { markRemovedPhotos, purgeDeletedPhotos } from './photo-storage.js';

// A transação remove dependências antes da conta, respeitando as chaves estrangeiras existentes.
// A fila de fotos não tem FK: sobrevive à conta e permite repetir falhas de remoção do disco.
const deleteAccountRecords = db.transaction(async (userId) => {
  await db.prepare('SELECT id FROM users WHERE id = ? FOR UPDATE').get(userId);
  await markRemovedPhotos(userId, []);
  await db
    .prepare(
      'DELETE FROM messages WHERE sender_id = ? OR match_id IN (SELECT id FROM matches WHERE user1 = ? OR user2 = ?)'
    )
    .run(userId, userId, userId);
  await db
    .prepare(
      'DELETE FROM call_history WHERE caller_id = ? OR receiver_id = ? OR match_id IN (SELECT id FROM matches WHERE user1 = ? OR user2 = ?)'
    )
    .run(userId, userId, userId, userId);
  for (const [table, first, second] of [
    ['likes', 'from_user', 'to_user'],
    ['dislikes', 'from_user', 'to_user'],
    ['blocks', 'blocker_id', 'blocked_id'],
    ['reports', 'reporter_id', 'reported_id'],
    ['matches', 'user1', 'user2'],
  ])
    await db.prepare(`DELETE FROM ${table} WHERE ${first} = ? OR ${second} = ?`).run(userId, userId);
  await db.prepare('DELETE FROM payment_logs WHERE user_id = ?').run(userId);
  await db.prepare('DELETE FROM play_purchases WHERE user_id = ?').run(userId);
  await db.prepare('DELETE FROM play_accounts WHERE user_id = ?').run(userId);
  await db.prepare('DELETE FROM subscriptions WHERE user_id = ?').run(userId);
  await db.prepare('DELETE FROM users WHERE id = ?').run(userId);
});

export async function deleteAccount(userId) {
  await deleteAccountRecords(userId);
  await purgeDeletedPhotos();
}
