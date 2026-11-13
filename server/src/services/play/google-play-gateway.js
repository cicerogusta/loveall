import { GoogleAuth, OAuth2Client } from 'google-auth-library';
import { BillingError } from './purchase-policy.js';

const publisherRoot = 'https://androidpublisher.googleapis.com/androidpublisher/v3/applications/';
export class GooglePlayGateway {
  constructor(config) {
    this.config = config;
    this.auth = new GoogleAuth({ scopes: ['https://www.googleapis.com/auth/androidpublisher'] });
    this.oidc = new OAuth2Client();
  }
  async request(path, method = 'GET', body) {
    try {
      const client = await this.auth.getClient();
      const result = await client.request({ url: publisherRoot + encodeURIComponent(this.config.packageName) + path,
        method, ...(body ? { data: body } : {}), timeout: 15000 });
      return result.data;
    } catch (error) {
      // Não registrar tokens, cabeçalhos ou o corpo do erro da biblioteca Google.
      if ([400, 404, 410].includes(error.response?.status)) throw new BillingError(400, 'Compra não reconhecida pela Google Play.');
      throw new BillingError(503, 'Não foi possível verificar a compra. Tente novamente.');
    }
  }
  getSubscription(token) { return this.request(`/purchases/subscriptionsv2/tokens/${encodeURIComponent(token)}`); }
  acknowledge(productId, token) {
    return this.request(`/purchases/subscriptions/${encodeURIComponent(productId)}/tokens/${encodeURIComponent(token)}:acknowledge`, 'POST', {});
  }
  async authenticatePush(authorization) {
    if (typeof authorization !== 'string' || !authorization.startsWith('Bearer ')) throw new BillingError(401, 'Notificação não autenticada.');
    try {
      const ticket = await this.oidc.verifyIdToken({ idToken: authorization.slice(7), audience: this.config.audience });
      const claims = ticket.getPayload();
      if (claims?.email !== this.config.pushEmail || claims?.email_verified !== true) throw new Error('Untrusted sender');
    } catch { throw new BillingError(401, 'Notificação não autenticada.'); }
  }
}
