import { MercadoPagoConfig, Preference } from 'mercadopago';
import crypto from 'crypto';
import db from '../database.js';
import { v4 as uuid } from 'uuid';

const client = new MercadoPagoConfig({
  accessToken: process.env.MERCADO_PAGO_ACCESS_TOKEN,
  options: { timeout: 5000 }
});

const PLANS = {
  basic: { name: 'Básico', price: 9.90, minutes: 60 },
  plus: { name: 'Plus', price: 19.90, minutes: 180 },
  premium: { name: 'Premium (Ilimitado)', price: 29.90, minutes: 999999 }
};

export async function createPaymentPreference(plan, userId, userEmail) {
  if (!PLANS[plan]) throw new Error('Plano inválido');

  const planData = PLANS[plan];
  const transactionId = uuid();

  try {
    const preference = new Preference(client);
    const result = await preference.create({
      body: {
        items: [
          {
            id: transactionId,
            title: `LoveAll - ${planData.name}`,
            description: `${planData.minutes === 999999 ? 'Minutos ilimitados' : planData.minutes + ' minutos'} de chamadas de áudio e vídeo por 30 dias`,
            picture_url: 'https://loveall.com/logo.png',
            category_id: 'call_minutes',
            quantity: 1,
            unit_price: planData.price,
            currency_id: 'BRL'
          }
        ],
        payer: {
          email: userEmail,
          name: 'LoveAll User'
        },
        back_urls: {
          success: `${process.env.FRONTEND_URL}/payment-success?tx_id=${transactionId}&plan=${plan}`,
          failure: `${process.env.FRONTEND_URL}/payment-failed?tx_id=${transactionId}`,
          pending: `${process.env.FRONTEND_URL}/payment-pending?tx_id=${transactionId}`
        },
        auto_return: 'approved',
        notification_url: `${process.env.FRONTEND_URL}/api/calls/mercado-pago/webhook`,
        external_reference: `user_${userId}_${transactionId}`,
        expires: false
      }
    });

    // Salvar transação no banco
    db.prepare(`
      INSERT INTO subscriptions (id, user_id, plan, transaction_id, payment_status, payment_url, payment_created_at)
      VALUES (?, ?, ?, ?, ?, ?, datetime('now'))
      ON CONFLICT(user_id) DO UPDATE SET
        transaction_id = ?, payment_status = 'pending', payment_url = ?, payment_created_at = datetime('now')
    `).run(
      uuid(), userId, plan, transactionId, 'pending', result.init_point,
      transactionId, result.init_point
    );

    // Log na tabela de pagamentos
    db.prepare(`
      INSERT INTO payment_logs (id, user_id, transaction_id, plan, amount, status)
      VALUES (?, ?, ?, ?, ?, ?)
    `).run(uuid(), userId, transactionId, plan, planData.price, 'pending');

    return {
      transaction_id: transactionId,
      payment_url: result.init_point,
      plan: plan,
      amount: planData.price
    };
  } catch (error) {
    console.error('Erro ao criar preferência de pagamento:', error);
    throw new Error('Falha ao iniciar pagamento. Tente novamente.');
  }
}

export function verifyWebhookSignature(body, signatureHeader) {
  if (!signatureHeader) return false;

  const signature = signatureHeader.split(',')[0];
  const timestamp = signatureHeader.split(',')[1];
  const version = signatureHeader.split(',')[2];

  if (!signature || !timestamp) return false;

  // Reconstruct para validação
  const secret = process.env.MERCADO_PAGO_WEBHOOK_TOKEN;
  const queryString = `request-id=${timestamp};timestamp=${Math.floor(Date.now() / 1000)};`;

  const computed = crypto
    .createHmac('sha256', secret)
    .update(queryString)
    .digest('hex');

  return signature === computed;
}

export function handlePaymentApproved(transactionId, userId, plan) {
  if (!PLANS[plan]) throw new Error('Plano inválido');

  const planData = PLANS[plan];

  try {
    // Atualizar subscription
    db.prepare(`
      UPDATE subscriptions
      SET plan = ?, call_minutes_remaining = ?, payment_status = 'approved', expires_at = datetime('now', '+30 days')
      WHERE user_id = ? AND transaction_id = ?
    `).run(plan, planData.minutes, userId, transactionId);

    // Atualizar is_premium no user
    db.prepare('UPDATE users SET is_premium = 1 WHERE id = ?').run(userId);

    // Log de aprovação
    db.prepare(`
      UPDATE payment_logs
      SET status = 'approved'
      WHERE transaction_id = ? AND user_id = ?
    `).run(transactionId, userId);

    return {
      success: true,
      message: `Plano ${planData.name} ativado com sucesso!`,
      plan: plan,
      minutes: planData.minutes
    };
  } catch (error) {
    console.error('Erro ao aprovar pagamento:', error);
    throw new Error('Erro ao ativar plano');
  }
}

export function handlePaymentFailed(transactionId, userId, errorMessage) {
  try {
    db.prepare(`
      UPDATE payment_logs
      SET status = 'failed', error_message = ?
      WHERE transaction_id = ? AND user_id = ?
    `).run(errorMessage || 'Pagamento recusado', transactionId, userId);

    db.prepare(`
      UPDATE subscriptions
      SET payment_status = 'failed'
      WHERE transaction_id = ? AND user_id = ?
    `).run(transactionId, userId);

    return { success: false, message: 'Pagamento foi recusado. Tente novamente.' };
  } catch (error) {
    console.error('Erro ao registrar falha de pagamento:', error);
  }
}

export function getPaymentStatus(transactionId, userId) {
  try {
    const sub = db.prepare(`
      SELECT plan, payment_status, call_minutes_remaining, expires_at
      FROM subscriptions
      WHERE transaction_id = ? AND user_id = ?
    `).get(transactionId, userId);

    if (!sub) return { status: 'not_found', message: 'Transação não encontrada' };

    return {
      status: sub.payment_status,
      plan: sub.plan,
      minutes: sub.call_minutes_remaining,
      expires_at: sub.expires_at
    };
  } catch (error) {
    console.error('Erro ao obter status de pagamento:', error);
    return { status: 'error', message: 'Erro ao buscar status' };
  }
}

export function getPlans() {
  return PLANS;
}
