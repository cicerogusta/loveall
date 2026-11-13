import { Router } from 'express';
import db from '../database.js';
import { authMiddleware } from '../middleware/auth.js';

const router = Router();
// Não vendemos benefícios futuros. Clientes próprios também não podem contornar a interface.
const unavailable = (_req, res) =>
  res.status(503).json({ error: 'Chamadas e compras estão indisponíveis nesta versão.' });
router.post('/mercado-pago/webhook', unavailable);
router.post('/subscribe', authMiddleware, unavailable);
router.post('/check', authMiddleware, unavailable);
router.post('/log', authMiddleware, unavailable);
router.get('/payment-status/:transactionId', authMiddleware, unavailable);
router.get('/plans', (_req, res) => res.json({ plans: {} }));
router.get('/my-plan', authMiddleware, async (req, res) => {
  const subscription = await db.prepare('SELECT * FROM subscriptions WHERE user_id = ?').get(req.user.id);
  res.json({ subscription: subscription || { plan: 'free', call_minutes_remaining: 0 } });
});
export default router;
