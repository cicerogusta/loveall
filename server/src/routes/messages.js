import { Router } from 'express';
import { randomUUID as uuid } from 'node:crypto';
import db from '../database.js';
import { requireAcceptedTerms } from '../middleware/terms.js';
import { authMiddleware } from '../middleware/auth.js';
import { contentFilter, rateLimiter } from '../middleware/safety.js';

const router = Router();

router.get('/:matchId', authMiddleware, async (req, res) => {
  const { matchId } = req.params;
  const userId = req.user.id;
  const page = Number(req.query.page ?? 1);
  const limit = Number(req.query.limit ?? 50);
  if (
    !Number.isInteger(page) ||
    page < 1 ||
    page > 100000 ||
    !Number.isInteger(limit) ||
    limit < 1 ||
    limit > 100
  )
    return res.status(400).json({ error: 'Paginação inválida.' });
  const offset = (page - 1) * limit;

  const match = await db
    .prepare('SELECT * FROM matches WHERE id = ? AND (user1 = ? OR user2 = ?) AND is_active = 1')
    .get(matchId, userId, userId);

  if (!match) {
    return res.status(404).json({ error: 'Conversa não encontrada' });
  }

  const messages = await db
    .prepare(
      `
    SELECT m.*, u.name as sender_name, u.photos as sender_photos
    FROM messages m
    JOIN users u ON u.id = m.sender_id
    WHERE m.match_id = ?
    ORDER BY m.created_at DESC
    LIMIT ? OFFSET ?
  `
    )
    .all(matchId, limit, offset);

  // Marcar como lidas
  await db
    .prepare('UPDATE messages SET is_read = 1 WHERE match_id = ? AND sender_id != ? AND is_read = 0')
    .run(matchId, userId);

  res.json({
    messages: messages.reverse().map((m) => ({
      ...m,
      sender_photos: JSON.parse(m.sender_photos || '[]'),
    })),
  });
});

router.post(
  '/:matchId',
  authMiddleware,
  requireAcceptedTerms,
  contentFilter,
  rateLimiter,
  async (req, res) => {
    const { matchId } = req.params;
    const { content, msg_type } = req.body;
    const userId = req.user.id;

    if (typeof content !== 'string' || !content.trim() || (msg_type !== undefined && msg_type !== 'text')) {
      return res.status(400).json({ error: 'Mensagem não pode ser vazia' });
    }

    if (content.length > 2000) {
      return res.status(400).json({ error: 'Mensagem muito longa (máx. 2000 caracteres)' });
    }

    const match = await db
      .prepare('SELECT * FROM matches WHERE id = ? AND (user1 = ? OR user2 = ?) AND is_active = 1')
      .get(matchId, userId, userId);

    if (!match) {
      return res.status(404).json({ error: 'Conversa não encontrada' });
    }

    const msgId = uuid();
    await db
      .prepare('INSERT INTO messages (id, match_id, sender_id, content, msg_type) VALUES (?, ?, ?, ?, ?)')
      .run(msgId, matchId, userId, content.trim(), msg_type || 'text');

    const message = await db.prepare('SELECT * FROM messages WHERE id = ?').get(msgId);

    res.status(201).json({ message });
  }
);

export default router;
