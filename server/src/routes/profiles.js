import { Router } from 'express';
import db from '../database.js';
import { requireAcceptedTerms } from '../middleware/terms.js';
import { authMiddleware } from '../middleware/auth.js';
import { checkBlocked } from '../middleware/safety.js';
import { validateProfilePatch } from '../validation/profile.js';
import { markRemovedPhotos, purgeDeletedPhotos } from '../services/photo-storage.js';

const router = Router();

router.put('/me', authMiddleware, requireAcceptedTerms, async (req, res) => {
  if (!(await validateProfilePatch(req.body, req.user)))
    return res.status(400).json({ error: 'Dados do perfil inválidos.' });
  const {
    name,
    bio,
    gender,
    gender_custom,
    looking_for,
    photos,
    city,
    max_distance,
    age_min,
    age_max,
    location_lat,
    location_lng,
  } = req.body;
  const userId = req.user.id;

  const updates = [];
  const params = [];

  if (name !== undefined) {
    updates.push('name = ?');
    params.push(name);
  }
  if (bio !== undefined) {
    updates.push('bio = ?');
    params.push(bio);
  }
  if (gender !== undefined) {
    updates.push('gender = ?');
    params.push(gender);
  }
  if (gender_custom !== undefined) {
    updates.push('gender_custom = ?');
    params.push(gender_custom);
  }
  if (looking_for !== undefined) {
    updates.push('looking_for = ?');
    params.push(JSON.stringify(looking_for));
  }
  if (photos !== undefined) {
    updates.push('photos = ?');
    params.push(JSON.stringify(photos));
  }
  if (city !== undefined) {
    updates.push('city = ?');
    params.push(city);
  }
  if (max_distance !== undefined) {
    updates.push('max_distance = ?');
    params.push(max_distance);
  }
  if (age_min !== undefined) {
    updates.push('age_min = ?');
    params.push(age_min);
  }
  if (age_max !== undefined) {
    updates.push('age_max = ?');
    params.push(age_max);
  }
  if (location_lat !== undefined) {
    updates.push('location_lat = ?');
    params.push(location_lat);
  }
  if (location_lng !== undefined) {
    updates.push('location_lng = ?');
    params.push(location_lng);
  }

  if (updates.length === 0) {
    return res.status(400).json({ error: 'Nenhum campo para atualizar' });
  }

  updates.push("updated_at = datetime('now')");
  params.push(userId);

  await db.transaction(async () => {
    const current = await db.prepare('SELECT * FROM users WHERE id = ? FOR UPDATE').get(userId);
    if (!current) return;
    current.photos = JSON.parse(current.photos || '[]');
    if (!(await validateProfilePatch(req.body, current))) throw new Error('Profile changed during update');
    await db.prepare(`UPDATE users SET ${updates.join(', ')} WHERE id = ?`).run(...params);
    if (photos !== undefined) {
      await markRemovedPhotos(userId, photos);
      for (const photo of photos)
        await db
          .prepare('UPDATE uploaded_photos SET attached = 1 WHERE reference = ? AND user_id = ?')
          .run(photo, userId);
    }
  })();
  await purgeDeletedPhotos();

  const user = await db.prepare('SELECT * FROM users WHERE id = ?').get(userId);
  const { password, ...safeUser } = user;
  safeUser.photos = JSON.parse(safeUser.photos || '[]');
  safeUser.looking_for = JSON.parse(safeUser.looking_for || '[]');

  res.json({ user: safeUser });
});

router.get('/discover', authMiddleware, async (req, res) => {
  const user = req.user;
  const lookingFor = user.looking_for;

  let genderFilter = '';
  const filterParams = [user.id, user.id, user.id, user.id, user.id, user.id];

  if (lookingFor.length > 0) {
    const placeholders = lookingFor.map(() => '?').join(',');
    genderFilter = `AND u.gender IN (${placeholders})`;
    filterParams.push(...lookingFor);
  }

  const profiles = await db
    .prepare(
      `
    SELECT u.id, u.name, u.birthdate, u.gender, u.gender_custom, u.bio, u.photos, u.city, u.is_verified
    FROM users u
    WHERE u.id != ?
      AND u.is_active = 1
      AND u.is_banned = 0
      AND u.id NOT IN (SELECT to_user FROM likes WHERE from_user = ?)
      AND u.id NOT IN (SELECT to_user FROM dislikes WHERE from_user = ?)
      AND u.id NOT IN (SELECT blocked_id FROM blocks WHERE blocker_id = ?)
      AND u.id NOT IN (SELECT blocker_id FROM blocks WHERE blocked_id = ?)
      AND u.id NOT IN (
        SELECT CASE WHEN user1 = ? THEN user2 ELSE user1 END
        FROM matches WHERE user1 = ? OR user2 = ?
      )
      ${genderFilter}
    ORDER BY RANDOM()
    LIMIT 20
  `
    )
    .all(...filterParams, user.id, user.id);

  const result = profiles.map((p) => ({
    ...p,
    birthdate: undefined,
    photos: JSON.parse(p.photos || '[]'),
    age: calculateAge(p.birthdate),
  }));

  res.json({ profiles: result });
});

router.get('/:userId', authMiddleware, checkBlocked, async (req, res) => {
  const profile = await db
    .prepare(
      'SELECT id, name, birthdate, gender, gender_custom, bio, photos, city, is_verified FROM users WHERE id = ? AND is_active = 1 AND is_banned = 0'
    )
    .get(req.params.userId);

  if (!profile) {
    return res.status(404).json({ error: 'Perfil não encontrado' });
  }

  profile.photos = JSON.parse(profile.photos || '[]');
  profile.age = calculateAge(profile.birthdate);
  delete profile.birthdate;
  res.json({ profile });
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
