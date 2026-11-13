import { Router } from 'express';
import multer from 'multer';
import rateLimit from 'express-rate-limit';
import sharp from 'sharp';
import { randomUUID } from 'node:crypto';
import db from '../database.js';
import { requireAcceptedTerms } from '../middleware/terms.js';
import { authMiddleware } from '../middleware/auth.js';
import { photoStore, maintainPhotos, purgeDeletedPhotos } from '../services/photo-storage.js';

const maxBytes = 4 * 1024 * 1024;
const maxStoredPhotos = 12;
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: maxBytes, files: 1, fields: 0, parts: 1 },
}).single('photo');
const router = Router();
const uploadLimiter = rateLimit({
  windowMs: 60_000,
  limit: 12,
  keyGenerator: (req) => req.user.id,
  message: { error: 'Aguarde um minuto antes de enviar mais fotos.' },
});

async function normalizePhoto(buffer) {
  const decoder = sharp(buffer, { limitInputPixels: 40_000_000, failOn: 'warning' });
  if ((await decoder.metadata()).format !== 'jpeg') throw new Error('Invalid format');
  return decoder
    .rotate()
    .resize(1600, 1600, { fit: 'inside', withoutEnlargement: true })
    .jpeg({ quality: 85 })
    .toBuffer();
}
const reservePhoto = db.transaction(async (userId, reference) => {
  // Serializa a cota por conta, incluindo uploads que ainda estão na rede.
  const user = await db
    .prepare('SELECT id FROM users WHERE id = ? AND is_active = 1 AND is_banned = 0 FOR UPDATE')
    .get(userId);
  if (!user) return 401;
  const { count } = await db
    .prepare('SELECT COUNT(*) AS count FROM uploaded_photos WHERE user_id = ?')
    .get(userId);
  if (count >= maxStoredPhotos) return 409;
  await db
    .prepare('INSERT INTO uploaded_photos (reference, user_id, ready) VALUES (?, ?, 0)')
    .run(reference, userId);
  return 201;
});
router.post('/me/photos', authMiddleware, requireAcceptedTerms, uploadLimiter, async (req, res) => {
  try {
    await new Promise((resolve, reject) => upload(req, res, (error) => (error ? reject(error) : resolve())));
  } catch (error) {
    return res
      .status(error.code === 'LIMIT_FILE_SIZE' ? 413 : 400)
      .json({ error: 'Selecione uma foto JPEG de até 4 MB.' });
  }
  if (req.file?.mimetype !== 'image/jpeg')
    return res.status(400).json({ error: 'Selecione uma foto JPEG válida.' });
  let image;
  try {
    image = await normalizePhoto(req.file.buffer);
  } catch {
    return res.status(400).json({ error: 'Selecione uma foto JPEG válida.' });
  }
  const filename = `${randomUUID()}.jpg`,
    reference = `/api/photos/${filename}`;
  await maintainPhotos();
  const status = await reservePhoto(req.user.id, reference);
  if (status !== 201)
    return res
      .status(status)
      .json({
        error: 'Não foi possível adicionar outra foto. Salve o perfil ou tente novamente mais tarde.',
      });
  try {
    await photoStore.put(filename, image);
    await db.prepare('UPDATE uploaded_photos SET ready = 1 WHERE reference = ?').run(reference);
    const visible = await db
      .prepare(
        `SELECT 1 FROM uploaded_photos p JOIN users u ON u.id = p.user_id
      WHERE p.reference = ? AND p.pending_delete = 0 AND u.is_active = 1 AND u.is_banned = 0`
      )
      .get(reference);
    if (!visible) throw new Error('Account or photo no longer available');
    return res.status(201).json({ photo: reference });
  } catch {
    // A reserva permanece se o storage falhar também na remoção. A manutenção repete.
    await db
      .prepare('UPDATE uploaded_photos SET ready = 1, pending_delete = 1 WHERE reference = ?')
      .run(reference);
    await purgeDeletedPhotos();
    return res.status(503).json({ error: 'Não foi possível salvar a foto. Tente novamente mais tarde.' });
  }
});
export default router;
