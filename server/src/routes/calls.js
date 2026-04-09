import { Router } from 'express';
import { v4 as uuid } from 'uuid';
import db from '../database.js';
import { authMiddleware } from '../middleware/auth.js';
import * as mercadopago from '../services/mercadopago.js';

const router = Router();

const PLANS = mercadopago.getPlans();

// Verificar se pode fazer chamada
router.post('/check', authMiddleware, (req, res) => {
  const { receiver_id, call_type } = req.body;
  const userId = req.user.id;

  const sub = db.prepare('SELECT * FROM subscriptions WHERE user_id = ?').get(userId);

  if (!sub || sub.plan === 'free' || sub.call_minutes_remaining <= 0) {
    return res.status(403).json({
      error: 'Plano necessário',
      message: 'Para fazer chamadas de áudio e vídeo, você precisa de um plano.',
      plans: PLANS
    });
  }

  // Verificar se há match ativo
  const match = db.prepare(`
    SELECT id FROM matches
    WHERE ((user1 = ? AND user2 = ?) OR (user1 = ? AND user2 = ?)) AND is_active = 1
  `).get(userId, receiver_id, receiver_id, userId);

  if (!match) {
    return res.status(403).json({ error: 'Você só pode ligar para seus matches' });
  }

  res.json({
    allowed: true,
    minutes_remaining: sub.call_minutes_remaining,
    match_id: match.id
  });
});

// Registrar chamada
router.post('/log', authMiddleware, (req, res) => {
  const { receiver_id, match_id, call_type, duration } = req.body;
  const userId = req.user.id;

  const durationMinutes = Math.ceil((duration || 0) / 60);

  if (durationMinutes > 0) {
    db.prepare(
      'UPDATE subscriptions SET call_minutes_remaining = MAX(0, call_minutes_remaining - ?) WHERE user_id = ?'
    ).run(durationMinutes, userId);
  }

  db.prepare(
    'INSERT INTO call_history (id, caller_id, receiver_id, match_id, call_type, duration, status) VALUES (?, ?, ?, ?, ?, ?, ?)'
  ).run(uuid(), userId, receiver_id, match_id, call_type, duration || 0, duration > 0 ? 'completed' : 'missed');

  res.json({ logged: true });
});

// Criar preferência de pagamento (NOVO - retorna URL de checkout)
router.post('/subscribe', authMiddleware, async (req, res) => {
  const { plan } = req.body;
  const userId = req.user.id;
  const userEmail = req.user.email;

  try {
    if (!PLANS[plan]) {
      return res.status(400).json({ error: 'Plano inválido', plans: PLANS });
    }

    const result = await mercadopago.createPaymentPreference(plan, userId, userEmail);

    res.json({
      transaction_id: result.transaction_id,
      payment_url: result.payment_url,
      plan: result.plan,
      amount: result.amount,
      message: 'Redirecionando para Mercado Pago...'
    });
  } catch (error) {
    console.error('Erro ao criar pagamento:', error);
    res.status(500).json({ error: error.message || 'Erro ao iniciar pagamento' });
  }
});

// Webhook do Mercado Pago (sem proteção de auth - é chamado pela API deles)
router.post('/mercado-pago/webhook', (req, res) => {
  try {
    const body = req.body;
    const signature = req.headers['x-signature'];

    // Validar assinatura (opcional - depende da configuração do MP)
    // if (!mercadopago.verifyWebhookSignature(body, signature)) {
    //   return res.status(401).json({ error: 'Assinatura inválida' });
    // }

    // Mercado Pago envia diferentes tipos de eventos
    if (body.type === 'payment') {
      const payment = body.data;
      const transactionId = payment.id.toString();
      const externalRef = payment.external_reference || '';

      // Extrair user_id do external_reference (formato: user_USERID_TXID)
      const userId = externalRef.split('_')[1];

      if (!userId) {
        console.warn('Webhook sem user_id válido:', externalRef);
        return res.status(200).json({ acknowledged: true });
      }

      if (payment.status === 'approved') {
        // Obter o plano e executar aprovação
        const sub = db.prepare('SELECT plan FROM subscriptions WHERE transaction_id = ?').get(transactionId);
        if (sub) {
          mercadopago.handlePaymentApproved(transactionId, userId, sub.plan);
        }
      } else if (payment.status === 'rejected' || payment.status === 'cancelled') {
        mercadopago.handlePaymentFailed(transactionId, userId, `Pagamento ${payment.status}`);
      }
    }

    res.json({ acknowledged: true });
  } catch (error) {
    console.error('Erro ao processar webhook:', error);
    res.status(500).json({ error: 'Erro ao processar webhook' });
  }
});

// Status do pagamento (protegido - apenas usuário autenticado pode ver seu status)
router.get('/payment-status/:transactionId', authMiddleware, (req, res) => {
  const { transactionId } = req.params;
  const userId = req.user.id;

  try {
    const status = mercadopago.getPaymentStatus(transactionId, userId);
    res.json(status);
  } catch (error) {
    console.error('Erro ao obter status:', error);
    res.status(500).json({ error: 'Erro ao buscar status de pagamento' });
  }
});

// Ver planos
router.get('/plans', (req, res) => {
  res.json({ plans: PLANS });
});

// Meu plano
router.get('/my-plan', authMiddleware, (req, res) => {
  const sub = db.prepare('SELECT * FROM subscriptions WHERE user_id = ?').get(req.user.id);
  res.json({ subscription: sub || { plan: 'free', call_minutes_remaining: 0 } });
});

export default router;
