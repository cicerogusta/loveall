import { Router } from 'express';
import rateLimit from 'express-rate-limit';
import db from '../database.js';
import { authMiddleware } from '../middleware/auth.js';
import { requireAcceptedTerms } from '../middleware/terms.js';
import { billingConfig } from '../config/billing.js';
import { GooglePlayGateway } from '../services/play/google-play-gateway.js';
import { PlayBillingService } from '../services/play/play-billing-service.js';
import { BillingError } from '../services/play/purchase-policy.js';

export function createBillingRouter(service, gateway) {
  const router = Router();
  const unavailable = (res) => res.status(503).json({ error: 'Assinaturas ainda não estão configuradas.' });
  const action = (work) => async (req, res) => {
    try {
      await work(req, res);
    } catch (error) {
      res
        .status(error instanceof BillingError ? error.status : 503)
        .json({
          error: error instanceof BillingError ? error.message : 'Não foi possível verificar a assinatura.',
        });
    }
  };
  router.post(
    '/notifications',
    action(async (req, res) => {
      if (!service) return unavailable(res);
      await gateway.authenticatePush(req.headers.authorization);
      await service.notification(req.body);
      res.status(204).end();
    })
  );
  router.get('/config', authMiddleware, async (_req, res) => {
    res.json(
      service
        ? await service.configuration(_req.user.id)
        : { enabled: false, product_ids: [], obfuscated_account_id: null }
    );
  });
  router.get(
    '/entitlements',
    authMiddleware,
    action(async (req, res) => {
      res.json(service ? await service.refreshUser(req.user.id) : { ad_free: false, expires_at: null });
    })
  );
  const limiter = rateLimit({
    windowMs: 60_000,
    limit: 15,
    keyGenerator: (req) => req.user.id,
    message: { error: 'Aguarde um momento antes de verificar outra compra.' },
  });
  router.post(
    '/verify',
    authMiddleware,
    requireAcceptedTerms,
    limiter,
    action(async (req, res) => {
      if (!service) return unavailable(res);
      res.json(await service.verify(req.user.id, req.body?.product_id, req.body?.purchase_token));
    })
  );
  return router;
}

const config = billingConfig();
const gateway = config.enabled ? new GooglePlayGateway(config) : null;
const service = config.enabled ? new PlayBillingService(db, gateway, config) : null;
if (service) {
  const job = setInterval(() => {
    service.reconcile().catch(() => console.error('[Billing] Reconciliação indisponível.'));
  }, 15 * 60_000);
  job.unref();
}
export default createBillingRouter(service, gateway);
