import { accountIdFor, tokenHash, evaluatePurchase, BillingError } from './purchase-policy.js';
import { TokenVault } from './token-vault.js';

const freshnessMs = 24 * 60 * 60 * 1000;
export class PlayBillingService {
  constructor(db, gateway, config, now = Date.now) {
    this.db = db;
    this.gateway = gateway;
    this.config = config;
    this.now = now;
    this.vault = new TokenVault(config.encryptionKey);
    this.locks = new Map();
  }
  async configuration(userId) {
    const accountId = accountIdFor(userId);
    await this.db
      .prepare('INSERT OR IGNORE INTO play_accounts (user_id, obfuscated_id) VALUES (?, ?)')
      .run(userId, accountId);
    return { enabled: true, product_ids: this.config.productIds, obfuscated_account_id: accountId };
  }
  async entitlement(userId) {
    const row = await this.db
      .prepare(
        `SELECT expires_at FROM play_purchases WHERE user_id = ? AND active = 1 AND acknowledged = 1 AND superseded = 0
      AND expires_at > ? AND verified_at > ? ORDER BY expires_at DESC LIMIT 1`
      )
      .get(userId, new Date(this.now()).toISOString(), this.now() - freshnessMs);
    return { ad_free: Boolean(row), expires_at: row?.expires_at || null };
  }
  async exclusive(hash, work) {
    const previous = this.locks.get(hash) || Promise.resolve();
    let release;
    const held = new Promise((resolve) => {
      release = resolve;
    });
    this.locks.set(hash, held);
    await previous;
    try {
      return await work();
    } finally {
      release();
      if (this.locks.get(hash) === held) this.locks.delete(hash);
    }
  }
  async verify(userId, productId, token) {
    if (
      typeof token !== 'string' ||
      token.length < 1 ||
      token.length > 4096 ||
      !this.config.productIds.includes(productId)
    ) {
      throw new BillingError(400, 'Compra inválida.');
    }
    return this.exclusive(tokenHash(token), async () => {
      const hash = tokenHash(token);
      const previous = await this.db
        .prepare('SELECT user_id, superseded FROM play_purchases WHERE token_hash = ?')
        .get(hash);
      if (previous && previous.user_id !== userId)
        throw new BillingError(409, 'Compra vinculada a outra conta.');
      if (previous?.superseded) return await this.entitlement(userId);
      const resource = await this.gateway.getSubscription(token);
      const decision = evaluatePurchase(resource, userId, productId, this.config.productIds, this.now());
      await this.save(userId, productId, token, decision, resource.linkedPurchaseToken);
      // A confirmação ocorre somente depois de verificar e registrar o direito no servidor.
      if (decision.active && !decision.acknowledged) {
        await this.gateway.acknowledge(productId, token);
        await this.db
          .prepare('UPDATE play_purchases SET acknowledged = 1 WHERE token_hash = ? AND user_id = ?')
          .run(hash, userId);
      }
      return await this.entitlement(userId);
    });
  }
  async save(userId, productId, token, decision, linkedToken) {
    await this.db.transaction(async () => {
      if (
        !(await this.db
          .prepare('SELECT 1 FROM users WHERE id = ? AND is_active = 1 AND is_banned = 0')
          .get(userId))
      )
        throw new BillingError(401, 'Conta indisponível.');
      if (linkedToken && decision.active) {
        if (linkedToken === token) throw new BillingError(400, 'Referência de compra inválida.');
        const linked = await this.db
          .prepare('SELECT user_id FROM play_purchases WHERE token_hash = ?')
          .get(tokenHash(linkedToken));
        if (linked && linked.user_id !== userId)
          throw new BillingError(409, 'Compra anterior vinculada a outra conta.');
        await this.db
          .prepare('UPDATE play_purchases SET active = 0, superseded = 1 WHERE token_hash = ?')
          .run(tokenHash(linkedToken));
      }
      await this.db
        .prepare(
          `INSERT INTO play_purchases (token_hash, user_id, product_id, token_cipher, state, expires_at, active, acknowledged, verified_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(token_hash) DO UPDATE SET
        product_id = excluded.product_id, token_cipher = excluded.token_cipher, state = excluded.state, expires_at = excluded.expires_at, active = excluded.active,
        acknowledged = excluded.acknowledged, verified_at = excluded.verified_at`
        )
        .run(
          tokenHash(token),
          userId,
          productId,
          this.vault.seal(token),
          decision.state,
          decision.expiresAt,
          Number(decision.active),
          Number(decision.acknowledged),
          this.now()
        );
    })();
  }
  async refreshUser(userId) {
    const rows = await this.db
      .prepare('SELECT * FROM play_purchases WHERE user_id = ? ORDER BY verified_at DESC LIMIT 20')
      .all(userId);
    for (const row of rows) {
      // A verificação explícita e as notificações continuam consultando a Google; evita repetir
      // imediatamente a mesma consulta durante a atualização da tela após uma compra confirmada.
      if (this.now() - row.verified_at >= 60_000) await this.refreshRow(row);
    }
    return await this.entitlement(userId);
  }
  async refreshRow(row) {
    if (row.superseded) return;
    try {
      await this.verify(row.user_id, row.product_id, this.vault.open(row.token_cipher));
    } catch (error) {
      if (error instanceof BillingError && error.status === 400) {
        await this.db
          .prepare('UPDATE play_purchases SET active = 0 WHERE token_hash = ?')
          .run(row.token_hash);
        return;
      }
      throw error;
    }
  }
  async notification(body) {
    const encoded = body?.message?.data;
    if (typeof encoded !== 'string' || encoded.length > 48000)
      throw new BillingError(400, 'Notificação inválida.');
    let event;
    try {
      event = JSON.parse(Buffer.from(encoded, 'base64').toString('utf8'));
    } catch {
      throw new BillingError(400, 'Notificação inválida.');
    }
    if (event.packageName !== this.config.packageName) throw new BillingError(400, 'Aplicativo divergente.');
    if (event.testNotification) return;
    const token =
      event.subscriptionNotification?.purchaseToken || event.voidedPurchaseNotification?.purchaseToken;
    if (typeof token !== 'string' || token.length > 4096)
      throw new BillingError(400, 'Notificação inválida.');
    const row = await this.db
      .prepare('SELECT * FROM play_purchases WHERE token_hash = ?')
      .get(tokenHash(token));
    if (row) return this.refreshRow(row);
    // Uma compra pendente pode concluir com o Android fechado. O vínculo deve vir da Google,
    // nunca de um user_id enviado na notificação, para também confirmar essa compra a tempo.
    const resource = await this.gateway.getSubscription(token);
    const accountId = resource.externalAccountIdentifiers?.obfuscatedExternalAccountId;
    if (typeof accountId !== 'string') return;
    const account = await this.db
      .prepare('SELECT user_id FROM play_accounts WHERE obfuscated_id = ?')
      .get(accountId);
    const product = resource.lineItems?.find((item) =>
      this.config.productIds.includes(item.productId)
    )?.productId;
    if (account && product) await this.verify(account.user_id, product, token);
  }
  async reconcile() {
    if (this.reconciling) return;
    this.reconciling = true;
    try {
      const rows = await this.db
        .prepare('SELECT * FROM play_purchases WHERE active = 1 OR verified_at > ?')
        .all(this.now() - 7 * freshnessMs);
      for (const row of rows) {
        try {
          await this.refreshRow(row);
        } catch {
          console.error('[Billing] Reconciliação pendente; nova tentativa será feita.');
        }
      }
    } finally {
      this.reconciling = false;
    }
  }
}
