import { Router } from 'express';
import { randomUUID as uuid } from 'node:crypto';
import db from '../database.js';
import { authMiddleware } from '../middleware/auth.js';

const router = Router();

// Denunciar usuário
router.post('/report', authMiddleware, async (req, res) => {
  const { reported_id, reason, description } = req.body;
  const reporter_id = req.user.id;

  if (
    typeof reported_id !== 'string' ||
    reported_id === reporter_id ||
    typeof reason !== 'string' ||
    (description !== undefined && (typeof description !== 'string' || description.length > 2000)) ||
    !(await db.prepare('SELECT 1 FROM users WHERE id = ?').get(reported_id))
  ) {
    return res.status(400).json({ error: 'Usuário e motivo são obrigatórios' });
  }

  const validReasons = [
    'fake_profile',
    'inappropriate_content',
    'harassment',
    'spam',
    'underage',
    'scam',
    'hate_speech',
    'violence',
    'other',
  ];

  if (!validReasons.includes(reason)) {
    return res.status(400).json({ error: 'Motivo de denúncia inválido' });
  }

  await db
    .prepare('INSERT INTO reports (id, reporter_id, reported_id, reason, description) VALUES (?, ?, ?, ?, ?)')
    .run(uuid(), reporter_id, reported_id, reason, description || '');

  // Auto-ban se tiver muitas denúncias
  const reportCount = await db
    .prepare(
      "SELECT COUNT(DISTINCT reporter_id) as count FROM reports WHERE reported_id = ? AND status = 'pending'"
    )
    .get(reported_id);

  if (reportCount.count >= 5) {
    await db
      .prepare('UPDATE users SET is_banned = 1, ban_reason = ? WHERE id = ?')
      .run('Banido automaticamente por múltiplas denúncias', reported_id);
  }

  res.json({ reported: true, message: 'Denúncia registrada. Você também pode bloquear este perfil.' });
});

// Bloquear usuário
router.post('/block', authMiddleware, async (req, res) => {
  const { blocked_id } = req.body;
  const blocker_id = req.user.id;

  if (
    typeof blocked_id !== 'string' ||
    blocked_id === blocker_id ||
    !(await db.prepare('SELECT 1 FROM users WHERE id = ?').get(blocked_id))
  ) {
    return res.status(400).json({ error: 'ID do usuário é obrigatório' });
  }

  await db
    .prepare('INSERT OR IGNORE INTO blocks (id, blocker_id, blocked_id) VALUES (?, ?, ?)')
    .run(uuid(), blocker_id, blocked_id);

  // Desativar match se existir
  await db
    .prepare(
      `
    UPDATE matches SET is_active = 0
    WHERE (user1 = ? AND user2 = ?) OR (user1 = ? AND user2 = ?)
  `
    )
    .run(blocker_id, blocked_id, blocked_id, blocker_id);

  res.json({ blocked: true });
});

// Desbloquear
router.delete('/block/:blockedId', authMiddleware, async (req, res) => {
  await db
    .prepare('DELETE FROM blocks WHERE blocker_id = ? AND blocked_id = ?')
    .run(req.user.id, req.params.blockedId);
  res.json({ unblocked: true });
});

// Listar bloqueados
router.get('/blocked', authMiddleware, async (req, res) => {
  const blocked = await db
    .prepare(
      `
    SELECT u.id, u.name, u.photos FROM blocks b
    JOIN users u ON u.id = b.blocked_id
    WHERE b.blocker_id = ?
  `
    )
    .all(req.user.id);

  res.json({
    blocked: blocked.map((b) => ({ ...b, photos: JSON.parse(b.photos || '[]') })),
  });
});

// Dicas de segurança
router.get('/tips', (req, res) => {
  res.json({
    tips: [
      {
        icon: '🔒',
        title: 'Proteja seus dados',
        text: 'Nunca compartilhe senhas, dados bancários ou documentos pessoais.',
      },
      {
        icon: '📍',
        title: 'Encontros seguros',
        text: 'Sempre marque o primeiro encontro em local público e movimentado.',
      },
      {
        icon: '👥',
        title: 'Avise alguém',
        text: 'Conte a um amigo ou familiar sobre seu encontro e compartilhe sua localização.',
      },
      {
        icon: '🚫',
        title: 'Denuncie',
        text: 'Se alguém fizer você se sentir desconfortável, denuncie imediatamente.',
      },
      {
        icon: '✅',
        title: 'Verifique o perfil',
        text: 'Prefira interagir com perfis verificados. Desconfie de fotos muito editadas.',
      },
      {
        icon: '🚗',
        title: 'Transporte próprio',
        text: 'Vá ao encontro com seu próprio transporte. Não aceite caronas de desconhecidos.',
      },
      {
        icon: '🍷',
        title: 'Moderação',
        text: 'Não exagere no consumo de bebidas alcoólicas no primeiro encontro.',
      },
      {
        icon: '📱',
        title: 'Mantenha contato no app',
        text: 'Não compartilhe seu número pessoal até se sentir seguro(a).',
      },
    ],
  });
});

export default router;
