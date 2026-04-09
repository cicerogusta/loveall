import { Router } from 'express';
import { v4 as uuid } from 'uuid';
import db from '../database.js';
import { authMiddleware } from '../middleware/auth.js';
import { checkBlocked } from '../middleware/safety.js';

const router = Router();

// Like ilimitado e gratuito!
router.post('/like', authMiddleware, checkBlocked, (req, res) => {
  const { to_user, is_super_like } = req.body;
  const from_user = req.user.id;

  if (from_user === to_user) {
    return res.status(400).json({ error: 'Você não pode curtir a si mesmo' });
  }

  const target = db.prepare('SELECT id FROM users WHERE id = ? AND is_active = 1').get(to_user);
  if (!target) {
    return res.status(404).json({ error: 'Usuário não encontrado' });
  }

  const existing = db.prepare('SELECT id FROM likes WHERE from_user = ? AND to_user = ?').get(from_user, to_user);
  if (existing) {
    return res.status(400).json({ error: 'Você já curtiu este perfil' });
  }

  db.prepare('INSERT INTO likes (id, from_user, to_user, is_super_like) VALUES (?, ?, ?, ?)').run(uuid(), from_user, to_user, is_super_like ? 1 : 0);

  // Verificar match mútuo
  const mutual = db.prepare('SELECT id FROM likes WHERE from_user = ? AND to_user = ?').get(to_user, from_user);

  let match = null;
  if (mutual) {
    const matchId = uuid();
    const [u1, u2] = [from_user, to_user].sort();
    const existingMatch = db.prepare('SELECT id FROM matches WHERE user1 = ? AND user2 = ?').get(u1, u2);

    if (!existingMatch) {
      db.prepare('INSERT INTO matches (id, user1, user2) VALUES (?, ?, ?)').run(matchId, u1, u2);
      match = { id: matchId, user1: u1, user2: u2 };
    }
  }

  res.json({
    liked: true,
    match: match ? {
      id: match.id,
      message: '🎉 É um match! Vocês se curtiram mutuamente!'
    } : null
  });
});

router.post('/dislike', authMiddleware, (req, res) => {
  const { to_user } = req.body;
  const from_user = req.user.id;

  db.prepare('INSERT OR IGNORE INTO dislikes (id, from_user, to_user) VALUES (?, ?, ?)').run(uuid(), from_user, to_user);

  res.json({ disliked: true });
});

router.get('/who-liked-me', authMiddleware, (req, res) => {
  const likes = db.prepare(`
    SELECT u.id, u.name, u.photos, u.birthdate, u.gender, u.is_verified, l.is_super_like, l.created_at
    FROM likes l
    JOIN users u ON u.id = l.from_user
    WHERE l.to_user = ?
      AND l.from_user NOT IN (SELECT to_user FROM likes WHERE from_user = ?)
      AND l.from_user NOT IN (SELECT blocked_id FROM blocks WHERE blocker_id = ?)
    ORDER BY l.created_at DESC
  `).all(req.user.id, req.user.id, req.user.id);

  res.json({
    likes: likes.map(l => ({
      ...l,
      photos: JSON.parse(l.photos || '[]'),
      age: calculateAge(l.birthdate)
    }))
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
