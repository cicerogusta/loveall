import Database from 'better-sqlite3';
import { sqliteAdapter } from '../../src/database/sqlite-adapter.js';
import { randomBytes } from 'node:crypto';
import { PlayBillingService } from '../../src/services/play/play-billing-service.js';
import { accountIdFor } from '../../src/services/play/purchase-policy.js';

export const product = 'ad_free_monthly';
export const now = Date.parse('2026-11-12T12:00:00Z');
export function fixture(t) {
  const db = new Database(':memory:');
  db.pragma('foreign_keys = ON');
  db.exec(`CREATE TABLE users (id TEXT PRIMARY KEY, is_active INTEGER DEFAULT 1, is_banned INTEGER DEFAULT 0);
    INSERT INTO users(id) VALUES ('alice'), ('bob');
    CREATE TABLE play_accounts (user_id TEXT PRIMARY KEY REFERENCES users(id), obfuscated_id TEXT UNIQUE);
    CREATE TABLE play_purchases (token_hash TEXT PRIMARY KEY, user_id TEXT REFERENCES users(id), product_id TEXT,
      token_cipher TEXT, state TEXT, expires_at TEXT, active INTEGER, acknowledged INTEGER, verified_at INTEGER,
      superseded INTEGER DEFAULT 0);`);
  t.after(() => db.close());
  const config = { enabled: true, productIds: [product], packageName: 'com.loveall.app', encryptionKey: randomBytes(32) };
  const resources = new Map();
  const gateway = {
    acknowledgements: 0, failAcknowledgement: false,
    async getSubscription(token) {
      if (!resources.has(token)) throw new Error('Simulated provider failure');
      return structuredClone(resources.get(token));
    },
    async acknowledge(_productId, token) {
      this.acknowledgements++;
      if (this.failAcknowledgement) throw new Error('Simulated acknowledgement outage');
      resources.get(token).acknowledgementState = 'ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED';
    },
  };
  const service = new PlayBillingService(sqliteAdapter(db), gateway, config, () => now);
  return { db, service, gateway, resources, config };
}
export function purchase(overrides = {}) {
  return { subscriptionState: 'SUBSCRIPTION_STATE_ACTIVE', acknowledgementState: 'ACKNOWLEDGEMENT_STATE_PENDING',
    externalAccountIdentifiers: { obfuscatedExternalAccountId: accountIdFor('alice') },
    lineItems: [{ productId: product, expiryTime: new Date(now + 30 * 86400000).toISOString() }], ...overrides };
}
export const notification = token => ({ message: { data: Buffer.from(JSON.stringify({ packageName: 'com.loveall.app',
  subscriptionNotification: { purchaseToken: token, notificationType: 2 } })).toString('base64') } });
