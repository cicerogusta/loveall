import jwt from 'jsonwebtoken';
import db from '../database.js';
import { jwtSecret } from '../config/security.js';

export function generateToken(userId) {
  return jwt.sign({ id: userId }, jwtSecret, { algorithm: 'HS256', expiresIn: '30d' });
}

export async function authMiddleware(req, res, next) {
  const token = req.headers.authorization?.replace('Bearer ', '');

  if (!token) {
    return res.status(401).json({ error: 'Token não fornecido' });
  }

  let decoded;
  try {
    decoded = jwt.verify(token, jwtSecret, { algorithms: ['HS256'] });
    if (typeof decoded.id !== 'string') return res.status(401).json({ error: 'Token inválido' });
  } catch {
    return res.status(401).json({ error: 'Token inválido' });
  }
  // Uma falha do banco não invalida a sessão: o Express trata a indisponibilidade.
  const user = await db
    .prepare('SELECT * FROM users WHERE id = ? AND is_banned = 0 AND is_active = 1')
    .get(decoded.id);
  if (!user) return res.status(401).json({ error: 'Usuário não encontrado ou banido' });
  await db.prepare("UPDATE users SET last_active = datetime('now') WHERE id = ?").run(user.id);
  req.user = {
    ...user,
    photos: JSON.parse(user.photos || '[]'),
    looking_for: JSON.parse(user.looking_for || '[]'),
  };
  next();
}
