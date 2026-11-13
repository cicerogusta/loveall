import { Router } from 'express';
import { randomUUID as uuid } from 'node:crypto';
import db from '../database.js';
import { requireAcceptedTerms } from '../middleware/terms.js';
import { authMiddleware } from '../middleware/auth.js';
import { checkBlocked } from '../middleware/safety.js';

const router = Router();

// Like ilimitado e gratuito!
router.post('/like', authMiddleware, requireAcceptedTerms, checkBlocked, async (req, res) => {
  const { to_user, is_super_like } = req.body;
  const from_user = req.user.id;

  if (
    typeof to_user !== 'string' ||
    to_user.length > 100 ||
    (is_super_like !== undefined && typeof is_super_like !== 'boolean')
  )
    return res.status(400).json({ error: 'Curtida inválida.' });
  if (from_user === to_user) {
    return res.status(400).json({ error: 'Você não pode curtir a si mesmo' });
  }

  const result = await db.transaction(async () => {
    // A ordem estável evita deadlock entre curtidas simultâneas do mesmo par.
    for (const id of [from_user, to_user].sort())
      await db.prepare('SELECT id FROM users WHERE id = ? FOR UPDATE').get(id);
    const blocked = await db
      .prepare(
        'SELECT 1 FROM blocks WHERE (blocker_id = ? AND blocked_id = ?) OR (blocker_id = ? AND blocked_id = ?)'
      )
      .get(from_user, to_user, to_user, from_user);
    if (blocked) return { status: 403, error: 'Interação bloqueada' };
    const target = await db
      .prepare('SELECT id FROM users WHERE id = ? AND is_active = 1 AND is_banned = 0')
      .get(to_user);
    if (!target) {
      return { status: 404, error: 'Usuário não encontrado' };
    }

    const existing = await db
      .prepare('SELECT id FROM likes WHERE from_user = ? AND to_user = ?')
      .get(from_user, to_user);
    if (existing) {
      return { status: 400, error: 'Você já curtiu este perfil' };
    }

    await db
      .prepare('INSERT INTO likes (id, from_user, to_user, is_super_like) VALUES (?, ?, ?, ?)')
      .run(uuid(), from_user, to_user, is_super_like ? 1 : 0);

    // Verificar match mútuo
    const mutual = await db
      .prepare('SELECT id FROM likes WHERE from_user = ? AND to_user = ?')
      .get(to_user, from_user);

    let match = null;
    if (mutual) {
      const matchId = uuid();
      const [u1, u2] = [from_user, to_user].sort();
      const existingMatch = await db
        .prepare('SELECT id FROM matches WHERE user1 = ? AND user2 = ?')
        .get(u1, u2);

      if (!existingMatch) {
        await db.prepare('INSERT INTO matches (id, user1, user2) VALUES (?, ?, ?)').run(matchId, u1, u2);
        match = { id: matchId, user1: u1, user2: u2 };
      }
    }

    return { match };
  })();
  if (result.error) return res.status(result.status).json({ error: result.error });
  const { match } = result;
  res.json({
    liked: true,
    match: match
      ? {
          id: match.id,
          message: '🎉 É um match! Vocês se curtiram mutuamente!',
        }
      : null,
  });
});

router.post('/dislike', authMiddleware, requireAcceptedTerms, async (req, res) => {
  const { to_user } = req.body;
  const from_user = req.user.id;

  if (
    typeof to_user !== 'string' ||
    to_user === from_user ||
    !(await db.prepare('SELECT 1 FROM users WHERE id = ?').get(to_user))
  )
    return res.status(400).json({ error: 'Perfil inválido.' });
  await db
    .prepare('INSERT OR IGNORE INTO dislikes (id, from_user, to_user) VALUES (?, ?, ?)')
    .run(uuid(), from_user, to_user);

  res.json({ disliked: true });
});

router.get('/who-liked-me', authMiddleware, async (req, res) => {
  const likes = await db
    .prepare(
      `
    SELECT u.id, u.name, u.photos, u.birthdate, u.gender, u.is_verified, l.is_super_like, l.created_at
    FROM likes l
    JOIN users u ON u.id = l.from_user
    WHERE l.to_user = ? AND u.is_active = 1 AND u.is_banned = 0
      AND l.from_user NOT IN (SELECT to_user FROM likes WHERE from_user = ?)
      AND l.from_user NOT IN (SELECT blocked_id FROM blocks WHERE blocker_id = ?)
      AND l.from_user NOT IN (SELECT blocker_id FROM blocks WHERE blocked_id = ?)
    ORDER BY l.created_at DESC
  `
    )
    .all(req.user.id, req.user.id, req.user.id, req.user.id);

  res.json({
    likes: likes.map((l) => ({
      ...l,
      birthdate: undefined,
      photos: JSON.parse(l.photos || '[]'),
      age: calculateAge(l.birthdate),
    })),
  });
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
