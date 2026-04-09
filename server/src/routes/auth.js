import { Router } from 'express';
import bcrypt from 'bcryptjs';
import { v4 as uuid } from 'uuid';
import db from '../database.js';
import { generateToken, authMiddleware } from '../middleware/auth.js';
import { ageVerification } from '../middleware/safety.js';

const router = Router();

// Validacoes
const isValidEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
const isValidName = (name) => name && name.trim().length >= 2 && name.trim().length <= 50 && !/\d/.test(name);
const isValidPassword = (pw) => pw && pw.length >= 8 && /[a-zA-Z]/.test(pw) && /\d/.test(pw);
const sanitize = (str) => str ? str.trim().replace(/<[^>]*>/g, '') : '';

const VALID_GENDERS = [
  'woman', 'man', 'non-binary', 'trans-woman', 'trans-man',
  'genderqueer', 'genderfluid', 'agender', 'bigender',
  'two-spirit', 'other', 'prefer-not-to-say'
];

router.post('/register', (req, res) => {
  try {
    let { email, password, name, birthdate, gender, gender_custom, looking_for } = req.body;

    // Sanitizar
    email = sanitize(email).toLowerCase();
    name = sanitize(name);
    gender_custom = sanitize(gender_custom);

    // Validacoes
    if (!email || !password || !name || !birthdate || !gender) {
      return res.status(400).json({ error: 'Todos os campos obrigatorios devem ser preenchidos' });
    }

    if (!isValidName(name)) {
      return res.status(400).json({ error: 'Nome invalido. Use entre 2 e 50 caracteres, sem numeros' });
    }

    if (!isValidEmail(email)) {
      return res.status(400).json({ error: 'Email invalido' });
    }

    if (!isValidPassword(password)) {
      return res.status(400).json({ error: 'Senha deve ter no minimo 8 caracteres, com pelo menos 1 letra e 1 numero' });
    }

    if (!ageVerification(birthdate)) {
      return res.status(400).json({ error: 'Voce deve ter pelo menos 18 anos para usar o LoveAll' });
    }

    if (gender === 'other' && !gender_custom) {
      return res.status(400).json({ error: 'Informe como voce se identifica' });
    }

    // Validar looking_for
    if (looking_for && !Array.isArray(looking_for)) {
      return res.status(400).json({ error: 'Formato invalido para preferencias' });
    }

    const existing = db.prepare('SELECT id FROM users WHERE email = ?').get(email);
    if (existing) {
      return res.status(400).json({ error: 'Este email ja esta cadastrado' });
    }

    const hashedPassword = bcrypt.hashSync(password, 12);
    const id = uuid();

    db.prepare(`
      INSERT INTO users (id, email, password, name, birthdate, gender, gender_custom, looking_for)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    `).run(id, email, hashedPassword, name, birthdate, gender, gender_custom || null, JSON.stringify(looking_for || []));

    db.prepare('INSERT INTO subscriptions (id, user_id, plan) VALUES (?, ?, ?)').run(uuid(), id, 'free');

    const token = generateToken(id);
    res.status(201).json({ token, user: { id, email, name, gender, gender_custom } });
  } catch (err) {
    console.error('[Auth] Erro no registro:', err);
    res.status(500).json({ error: 'Erro interno do servidor' });
  }
});

router.post('/login', (req, res) => {
  try {
    let { email, password } = req.body;
    email = sanitize(email).toLowerCase();

    if (!email || !password) {
      return res.status(400).json({ error: 'Email e senha sao obrigatorios' });
    }

    const user = db.prepare('SELECT * FROM users WHERE email = ? AND is_banned = 0').get(email);
    if (!user) {
      return res.status(401).json({ error: 'Email ou senha incorretos' });
    }

    if (!bcrypt.compareSync(password, user.password)) {
      return res.status(401).json({ error: 'Email ou senha incorretos' });
    }

    const token = generateToken(user.id);
    res.json({
      token,
      user: {
        id: user.id,
        email: user.email,
        name: user.name,
        gender: user.gender,
        gender_custom: user.gender_custom,
        photos: JSON.parse(user.photos || '[]'),
        bio: user.bio,
        is_verified: user.is_verified,
        is_premium: user.is_premium
      }
    });
  } catch (err) {
    console.error('[Auth] Erro no login:', err);
    res.status(500).json({ error: 'Erro interno do servidor' });
  }
});

router.get('/me', authMiddleware, (req, res) => {
  const { password, ...user } = req.user;
  res.json({ user });
});

export default router;
