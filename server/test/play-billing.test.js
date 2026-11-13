import { test } from 'node:test';
import assert from 'node:assert/strict';
import { randomBytes } from 'node:crypto';
import { fixture, purchase, product, now, notification } from './helpers/play.js';
import { tokenHash } from '../src/services/play/purchase-policy.js';
import { TokenVault } from '../src/services/play/token-vault.js';
import { GooglePlayGateway } from '../src/services/play/google-play-gateway.js';
import { billingConfig } from '../src/config/billing.js';
import { startApi } from './helpers/api.js';

test('verified subscription is acknowledged once, encrypted and restored idempotently', async t => {
  const { service, resources, gateway, db } = fixture(t);
  resources.set('google-token', purchase());
  assert.equal((await service.verify('alice', product, 'google-token')).ad_free, true);
  assert.equal((await service.verify('alice', product, 'google-token')).ad_free, true);
  assert.equal(gateway.acknowledgements, 1);
  const rows = db.prepare('SELECT * FROM play_purchases').all();
  assert.equal(rows.length, 1);
  assert.ok(!JSON.stringify(rows).includes('google-token'));
  assert.equal((await service.refreshUser('alice')).ad_free, true);
});

test('a valid purchase cannot be transferred or replayed against another account', async t => {
  const { service, resources } = fixture(t);
  resources.set('token', purchase());
  await assert.rejects(service.verify('bob', product, 'token'), error => error.status === 403);
  await service.verify('alice', product, 'token');
  await assert.rejects(service.verify('bob', product, 'token'), error => error.status === 409);
  assert.equal((await service.entitlement('bob')).ad_free, false);
});

test('unknown products and mismatched Google product identifiers never grant access', async t => {
  const { service, resources } = fixture(t);
  await assert.rejects(service.verify('alice', 'unknown', 'token'), error => error.status === 400);
  resources.set('token', purchase({ lineItems: [{ productId: 'another-product', expiryTime: new Date(now + 86400000).toISOString() }] }));
  await assert.rejects(service.verify('alice', product, 'token'), error => error.status === 400);
  assert.equal((await service.entitlement('alice')).ad_free, false);
});

for (const state of ['SUBSCRIPTION_STATE_PENDING', 'SUBSCRIPTION_STATE_ON_HOLD', 'SUBSCRIPTION_STATE_PAUSED', 'SUBSCRIPTION_STATE_EXPIRED', 'SUBSCRIPTION_STATE_PENDING_PURCHASE_CANCELED']) {
  test(`state ${state} does not grant or acknowledge benefits`, async t => {
    const { service, resources, gateway } = fixture(t);
    resources.set('token', purchase({ subscriptionState: state }));
    assert.equal((await service.verify('alice', product, 'token')).ad_free, false);
    assert.equal(gateway.acknowledgements, 0);
  });
}

test('cancellation preserves prepaid access only until expiry; grace period keeps access', async t => {
  const { service, resources } = fixture(t);
  resources.set('cancelled', purchase({ subscriptionState: 'SUBSCRIPTION_STATE_CANCELED' }));
  assert.equal((await service.verify('alice', product, 'cancelled')).ad_free, true);
  resources.set('cancelled', purchase({ subscriptionState: 'SUBSCRIPTION_STATE_CANCELED', lineItems: [{ productId: product, expiryTime: new Date(now - 1).toISOString() }] }));
  assert.equal((await service.verify('alice', product, 'cancelled')).ad_free, false);
  resources.set('grace', purchase({ subscriptionState: 'SUBSCRIPTION_STATE_IN_GRACE_PERIOD' }));
  assert.equal((await service.verify('alice', product, 'grace')).ad_free, true);
});

test('acknowledgement failure can be retried without granting unconfirmed access', async t => {
  const { service, resources, gateway } = fixture(t);
  resources.set('token', purchase()); gateway.failAcknowledgement = true;
  await assert.rejects(service.verify('alice', product, 'token'));
  assert.equal((await service.entitlement('alice')).ad_free, false);
  gateway.failAcknowledgement = false;
  assert.equal((await service.verify('alice', product, 'token')).ad_free, true);
});

test('duplicate or delayed notifications fetch current Google status and revoke expired access', async t => {
  const { service, resources } = fixture(t);
  resources.set('token', purchase()); await service.verify('alice', product, 'token');
  resources.set('token', purchase({ subscriptionState: 'SUBSCRIPTION_STATE_EXPIRED' }));
  await service.notification(notification('token'));
  await service.notification(notification('token'));
  assert.equal((await service.entitlement('alice')).ad_free, false);
  resources.set('unknown', purchase({ externalAccountIdentifiers: { obfuscatedExternalAccountId: 'unmapped-account' } }));
  await service.notification(notification('unknown'));
  await assert.rejects(service.notification({ message: { data: 'broken' } }), error => error.status === 400);
});

test('completion while Android is closed is verified using the Google account link and acknowledged', async t => {
  const { service, resources, gateway } = fixture(t);
  await service.configuration('alice');
  resources.set('completed-offline', purchase());
  await service.notification(notification('completed-offline'));
  assert.equal((await service.entitlement('alice')).ad_free, true);
  assert.equal(gateway.acknowledgements, 1);
});

test('pending replacement keeps previous access; completed replacement cannot be resurrected by old token', async t => {
  const { service, resources, db } = fixture(t);
  resources.set('old', purchase()); await service.verify('alice', product, 'old');
  resources.set('new', purchase({ subscriptionState: 'SUBSCRIPTION_STATE_PENDING', linkedPurchaseToken: 'old' }));
  assert.equal((await service.verify('alice', product, 'new')).ad_free, true);
  resources.set('new', purchase({ linkedPurchaseToken: 'old' })); await service.verify('alice', product, 'new');
  assert.equal(db.prepare('SELECT superseded FROM play_purchases WHERE token_hash = ?').get(tokenHash('old')).superseded, 1);
  resources.set('new', purchase({ subscriptionState: 'SUBSCRIPTION_STATE_EXPIRED' }));
  await service.notification(notification('new'));
  assert.equal((await service.verify('alice', product, 'old')).ad_free, false);
});

test('unverified stale records do not grant access', async t => {
  const { service, resources, db } = fixture(t);
  resources.set('token', purchase()); await service.verify('alice', product, 'token');
  db.prepare('UPDATE play_purchases SET verified_at = ?').run(now - 25 * 3600000);
  assert.equal((await service.entitlement('alice')).ad_free, false);
});

test('token encryption detects tampering and requires the original key', () => {
  const key = randomBytes(32), vault = new TokenVault(key);
  const sealed = vault.seal('sensitive-token');
  assert.equal(vault.open(sealed), 'sensitive-token');
  assert.throws(() => new TokenVault(randomBytes(32)).open(sealed));
  const tampered = Buffer.from(sealed, 'base64'); tampered[30] ^= 1;
  assert.throws(() => vault.open(tampered.toString('base64')));
});

test('push notifications require correct OIDC audience, verified email and trusted service account', async () => {
  const gateway = new GooglePlayGateway({ audience: 'https://api.example.test/api/billing/notifications', pushEmail: 'push@project.iam.gserviceaccount.com' });
  let receivedAudience;
  gateway.oidc.verifyIdToken = async options => { receivedAudience = options.audience; return { getPayload: () => ({ email: 'push@project.iam.gserviceaccount.com', email_verified: true }) }; };
  await gateway.authenticatePush('Bearer signed-token');
  assert.equal(receivedAudience, gateway.config.audience);
  await assert.rejects(gateway.authenticatePush(undefined), error => error.status === 401);
  gateway.oidc.verifyIdToken = async () => ({ getPayload: () => ({ email: 'attacker@example.test', email_verified: true }) });
  await assert.rejects(gateway.authenticatePush('Bearer token'), error => error.status === 401);
  gateway.oidc.verifyIdToken = async () => { throw new Error('Invalid signature'); };
  await assert.rejects(gateway.authenticatePush('Bearer token'), error => error.status === 401);
});

test('billing stays disabled without account configuration and cannot be enabled partially', async t => {
  assert.equal(billingConfig({}).enabled, false);
  assert.throws(() => billingConfig({ PLAY_BILLING_ENABLED: 'true' }));
  const api = await startApi(t, { PLAY_BILLING_ENABLED: 'false' });
  const account = await api.register('Billing');
  assert.equal((await api.request('/api/billing/config')).status, 401);
  const config = await (await api.request('/api/billing/config', { token: account.token })).json();
  assert.equal(config.enabled, false);
  assert.deepEqual(await (await api.request('/api/billing/entitlements', { token: account.token })).json(), { ad_free: false, expires_at: null });
  assert.equal((await api.request('/api/billing/verify', { method: 'POST', token: account.token, body: { product_id: product, purchase_token: 'fake' } })).status, 503);
  assert.equal((await api.request('/api/billing/notifications', { method: 'POST', body: notification('fake') })).status, 503);
});
