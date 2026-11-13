import { createHash } from 'node:crypto';

export const accountIdFor = userId => createHash('sha256').update(userId).digest('hex');
export const tokenHash = token => createHash('sha256').update(token).digest('hex');
export class BillingError extends Error {
  constructor(status, message) { super(message); this.status = status; }
}

const accessStates = new Set(['SUBSCRIPTION_STATE_ACTIVE', 'SUBSCRIPTION_STATE_IN_GRACE_PERIOD', 'SUBSCRIPTION_STATE_CANCELED']);
export function evaluatePurchase(resource, userId, productId, products, now) {
  if (!products.includes(productId)) throw new BillingError(400, 'Produto não disponível.');
  if (resource.externalAccountIdentifiers?.obfuscatedExternalAccountId !== accountIdFor(userId)) {
    throw new BillingError(403, 'A compra não pertence a esta conta.');
  }
  const line = resource.lineItems?.filter(x => x.productId === productId)
    .sort((a, b) => Date.parse(b.expiryTime) - Date.parse(a.expiryTime))[0];
  if (!line) throw new BillingError(400, 'Produto divergente da compra.');
  const expires = Date.parse(line.expiryTime);
  const active = accessStates.has(resource.subscriptionState) && Number.isFinite(expires) && expires > now;
  return { active, state: resource.subscriptionState, expiresAt: Number.isFinite(expires) ? new Date(expires).toISOString() : null,
    acknowledged: resource.acknowledgementState === 'ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED' };
}
