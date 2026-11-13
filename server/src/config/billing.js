import fs from 'node:fs';

export function billingConfig(env = process.env) {
  if (env.PLAY_BILLING_ENABLED !== 'true') return { enabled: false, productIds: [] };
  const productIds = [...new Set((env.PLAY_AD_FREE_PRODUCT_IDS || '').split(',').map(x => x.trim()).filter(Boolean))];
  const packageName = env.PLAY_PACKAGE_NAME || 'com.loveall.app';
  const encryptionKey = Buffer.from(env.PLAY_TOKEN_ENCRYPTION_KEY || '', 'base64');
  if (!productIds.length || productIds.length > 20 || productIds.some(x => !/^[a-z0-9][a-z0-9._]{0,99}$/.test(x)) ||
      !/^[a-zA-Z][\w]*(\.[a-zA-Z][\w]*)+$/.test(packageName) || encryptionKey.length !== 32 ||
      !env.GOOGLE_APPLICATION_CREDENTIALS || !fs.existsSync(env.GOOGLE_APPLICATION_CREDENTIALS) ||
      !/^https:\/\/[^\s]+$/.test(env.PLAY_RTDN_AUDIENCE || '') ||
      !/^[^\s@]+@[^\s@]+\.gserviceaccount\.com$/.test(env.PLAY_RTDN_SERVICE_ACCOUNT_EMAIL || '')) {
    throw new Error('Configuração Play Billing incompleta: confira produtos, pacote, credencial, chave de cifragem e RTDN.');
  }
  return { enabled: true, productIds, packageName, encryptionKey,
    audience: env.PLAY_RTDN_AUDIENCE, pushEmail: env.PLAY_RTDN_SERVICE_ACCOUNT_EMAIL };
}
