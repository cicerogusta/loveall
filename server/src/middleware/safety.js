import db from '../database.js';

const BANNED_WORDS = [
  'pix', 'whatsapp', 'wpp', 'zap', 'telegram',
  'conta bancária', 'transferência', 'dinheiro',
  'nudes', 'sexo oral', 'programa',
];

const SPAM_WINDOW_MS = 60000;
const MAX_MESSAGES_PER_MINUTE = 20;
const messageTimestamps = new Map();

export function contentFilter(req, res, next) {
  const { content } = req.body;
  if (!content) return next();

  const lower = content.toLowerCase();

  for (const word of BANNED_WORDS) {
    if (lower.includes(word)) {
      return res.status(400).json({
        error: 'Mensagem bloqueada',
        detail: 'Sua mensagem contém conteúdo não permitido. Por segurança, não compartilhe dados pessoais ou financeiros pelo chat.'
      });
    }
  }

  next();
}

export function rateLimiter(req, res, next) {
  const userId = req.user?.id;
  if (!userId) return next();

  const now = Date.now();
  const timestamps = messageTimestamps.get(userId) || [];
  const recent = timestamps.filter(t => now - t < SPAM_WINDOW_MS);

  if (recent.length >= MAX_MESSAGES_PER_MINUTE) {
    return res.status(429).json({
      error: 'Muitas mensagens',
      detail: 'Você está enviando mensagens rápido demais. Aguarde um momento.'
    });
  }

  recent.push(now);
  messageTimestamps.set(userId, recent);
  next();
}

export function checkBlocked(req, res, next) {
  const userId = req.user?.id;
  const targetId = req.params.userId || req.body.to_user || req.body.receiver_id;

  if (!userId || !targetId) return next();

  const block = db.prepare(
    'SELECT id FROM blocks WHERE (blocker_id = ? AND blocked_id = ?) OR (blocker_id = ? AND blocked_id = ?)'
  ).get(userId, targetId, targetId, userId);

  if (block) {
    return res.status(403).json({ error: 'Interação bloqueada' });
  }

  next();
}

export function ageVerification(birthdate) {
  const birth = new Date(birthdate);
  const today = new Date();
  let age = today.getFullYear() - birth.getFullYear();
  const monthDiff = today.getMonth() - birth.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
    age--;
  }
  return age >= 18;
}
