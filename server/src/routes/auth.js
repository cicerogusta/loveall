import { Router } from 'express';
import bcrypt from 'bcryptjs';
import { randomUUID as uuid } from 'node:crypto';
import db from '../database.js';
import { generateToken, authMiddleware } from '../middleware/auth.js';
import {
  validName,
  validPassword,
  validBirthdate,
  validPreferences,
  genders,
  legalVersion,
} from '../validation/profile.js';
import { deleteAccount } from '../services/account-deletion.js';

const router = Router();

// Validacoes
const isValidEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
const sanitize = (value) => (typeof value === 'string' ? value.trim().replace(/<[^>]*>/g, '') : '');

router.post('/register', async (req, res) => {
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

    if (!validName(name)) {
      return res.status(400).json({ error: 'Nome invalido. Use entre 2 e 50 caracteres, sem numeros' });
    }

    if (!isValidEmail(email)) {
      return res.status(400).json({ error: 'Email invalido' });
    }

    if (!validPassword(password)) {
      return res
        .status(400)
        .json({ error: 'Senha deve ter no minimo 8 caracteres, com pelo menos 1 letra e 1 numero' });
    }

    if (!validBirthdate(birthdate)) {
      return res.status(400).json({ error: 'Voce deve ter pelo menos 18 anos para usar o LoveAll' });
    }

    if (gender === 'other' && !gender_custom) {
      return res.status(400).json({ error: 'Informe como voce se identifica' });
    }

    if (!genders.has(gender) || gender_custom.length > 100 || req.body.terms_version !== legalVersion) {
      return res.status(400).json({ error: 'Confira a identidade e aceite os termos atuais.' });
    }

    // Validar looking_for
    if (!validPreferences(looking_for ?? [])) {
      return res.status(400).json({ error: 'Formato invalido para preferencias' });
    }

    const existing = await db.prepare('SELECT id FROM users WHERE email = ?').get(email);
    if (existing) {
      return res.status(400).json({ error: 'Este email ja esta cadastrado' });
    }

    const hashedPassword = bcrypt.hashSync(password, 12);
    const id = uuid();

    await db.transaction(async () => {
      await db
        .prepare(
          `
        INSERT INTO users (id, email, password, name, birthdate, gender, gender_custom, looking_for)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
      `
        )
        .run(
          id,
          email,
          hashedPassword,
          name,
          birthdate,
          gender,
          gender_custom || null,
          JSON.stringify(looking_for || [])
        );

      await db
        .prepare('INSERT INTO subscriptions (id, user_id, plan) VALUES (?, ?, ?)')
        .run(uuid(), id, 'free');

      await db
        .prepare("UPDATE users SET terms_version = ?, terms_accepted_at = datetime('now') WHERE id = ?")
        .run(legalVersion, id);
    })();

    const token = generateToken(id);
    res
      .status(201)
      .json({ token, user: { id, email, name, gender, gender_custom, terms_version: legalVersion } });
  } catch (err) {
    if (err.code === '23505' || err.code === 'SQLITE_CONSTRAINT_UNIQUE')
      return res.status(400).json({ error: 'Este email ja esta cadastrado' });
    console.error('[Auth] Erro no registro:', err.name);
    res.status(500).json({ error: 'Erro interno do servidor' });
  }
});

router.post('/login', async (req, res) => {
  try {
    let { email, password } = req.body;
    email = sanitize(email).toLowerCase();

    if (!email || typeof password !== 'string' || password.length === 0 || password.length > 128) {
      return res.status(400).json({ error: 'Email e senha sao obrigatorios' });
    }

    const user = await db
      .prepare('SELECT * FROM users WHERE email = ? AND is_banned = 0 AND is_active = 1')
      .get(email);
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
        terms_version: user.terms_version,
        gender: user.gender,
        gender_custom: user.gender_custom,
        photos: JSON.parse(user.photos || '[]'),
        bio: user.bio,
        is_verified: user.is_verified,
        is_premium: user.is_premium,
      },
    });
  } catch (err) {
    console.error('[Auth] Erro no login:', err.name);
    res.status(500).json({ error: 'Erro interno do servidor' });
  }
});

router.get('/me', authMiddleware, (req, res) => {
  const { password, ...user } = req.user;
  res.json({ user });
});

router.post('/terms', authMiddleware, async (req, res) => {
  if (req.body?.terms_version !== legalVersion)
    return res.status(400).json({ error: 'Versão de termos inválida.' });
  await db
    .prepare("UPDATE users SET terms_version = ?, terms_accepted_at = datetime('now') WHERE id = ?")
    .run(legalVersion, req.user.id);
  const { password, ...user } = req.user;
  res.json({ user: { ...user, terms_version: legalVersion } });
});

// Reautenticação evita exclusão acidental por uma sessão esquecida no aparelho.
router.delete('/me', authMiddleware, async (req, res) => {
  const password = req.body?.password;
  if (
    typeof password !== 'string' ||
    password.length > 128 ||
    !bcrypt.compareSync(password, req.user.password)
  ) {
    return res.status(403).json({ error: 'Confirme sua senha para excluir a conta.' });
  }
  await deleteAccount(req.user.id);
  res.status(204).end();
});

export default router;
