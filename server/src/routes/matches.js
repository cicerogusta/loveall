import { Router } from 'express';
import db from '../database.js';
import { authMiddleware } from '../middleware/auth.js';

const router = Router();

router.get('/', authMiddleware, async (req, res) => {
  const userId = req.user.id;

  const matches = await db
    .prepare(
      `
    SELECT m.id as match_id, m.created_at as matched_at,
      CASE WHEN m.user1 = ? THEN m.user2 ELSE m.user1 END as partner_id
    FROM matches m
    WHERE (m.user1 = ? OR m.user2 = ?) AND m.is_active = 1
    ORDER BY m.created_at DESC
  `
    )
    .all(userId, userId, userId);

  const result = await Promise.all(
    matches.map(async (match) => {
      const partner = await db
        .prepare(
          'SELECT id, name, photos, birthdate, gender, gender_custom, bio, city, is_verified, last_active FROM users WHERE id = ?'
        )
        .get(match.partner_id);

      const lastMessage = await db
        .prepare(
          'SELECT content, created_at, sender_id FROM messages WHERE match_id = ? ORDER BY created_at DESC LIMIT 1'
        )
        .get(match.match_id);

      const unread = await db
        .prepare(
          'SELECT COUNT(*) as count FROM messages WHERE match_id = ? AND sender_id != ? AND is_read = 0'
        )
        .get(match.match_id, userId);

      return {
        match_id: match.match_id,
        matched_at: match.matched_at,
        partner: partner
          ? {
              ...partner,
              birthdate: undefined,
              photos: JSON.parse(partner.photos || '[]'),
              age: calculateAge(partner.birthdate),
            }
          : null,
        last_message: lastMessage || null,
        unread_count: unread?.count || 0,
      };
    })
  );

  res.json({ matches: result });
});

router.delete('/:matchId', authMiddleware, async (req, res) => {
  const { matchId } = req.params;
  const userId = req.user.id;

  const match = await db
    .prepare('SELECT * FROM matches WHERE id = ? AND (user1 = ? OR user2 = ?)')
    .get(matchId, userId, userId);

  if (!match) {
    return res.status(404).json({ error: 'Match não encontrado' });
  }

  await db.prepare('UPDATE matches SET is_active = 0 WHERE id = ?').run(matchId);
  res.json({ unmatched: true });
});

function calculateAge(birthdate) {
  const birth = new Date(birthdate);
  const today = new Date();
  let age = today.getFullYear() - birth.getFullYear();
  const m = today.getMonth() - birth.getMonth();
  if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) age--;
  return age;
}

export default router;
