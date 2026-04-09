import jwt from 'jsonwebtoken';
import db from '../database.js';

const JWT_SECRET = process.env.JWT_SECRET || 'loveall_secret';

export function generateToken(userId) {
  return jwt.sign({ id: userId }, JWT_SECRET, { expiresIn: '30d' });
}

export function authMiddleware(req, res, next) {
  const token = req.headers.authorization?.replace('Bearer ', '');

  if (!token) {
    return res.status(401).json({ error: 'Token não fornecido' });
  }

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    const user = db.prepare('SELECT * FROM users WHERE id = ? AND is_banned = 0').get(decoded.id);

    if (!user) {
      return res.status(401).json({ error: 'Usuário não encontrado ou banido' });
    }

    db.prepare('UPDATE users SET last_active = datetime(\'now\') WHERE id = ?').run(user.id);

    req.user = {
      ...user,
      photos: JSON.parse(user.photos || '[]'),
      looking_for: JSON.parse(user.looking_for || '[]')
    };
    next();
  } catch {
    return res.status(401).json({ error: 'Token inválido' });
  }
}
