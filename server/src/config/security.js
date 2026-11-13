// A configuração é validada antes de aceitar conexões; não existe segredo alternativo.
const configuredSecret = process.env.JWT_SECRET;
const knownDefaults = new Set(['loveall_secret', 'MUDE_ESTA_CHAVE_EM_PRODUCAO']);
if (typeof configuredSecret !== 'string' || configuredSecret.trim().length < 32 || knownDefaults.has(configuredSecret)) {
  throw new Error('Configure JWT_SECRET com um segredo exclusivo de pelo menos 32 caracteres.');
}
export const jwtSecret = configuredSecret;
