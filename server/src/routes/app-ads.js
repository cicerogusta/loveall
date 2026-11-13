import { Router } from 'express';

// Identificador público do vendedor. Sem configuração, não publica uma linha
// fictícia nem declara que uma conta desconhecida pode monetizar o aplicativo.
export function createAppAdsRouter(publisherId = '') {
  if (publisherId && !/^pub-\d{16}$/.test(publisherId)) {
    throw new Error('ADMOB_PUBLISHER_ID deve ter o formato pub- seguido de 16 dígitos.');
  }
  const router = Router();
  router.get('/app-ads.txt', (_req, res) => {
    res.set('Cache-Control', 'public, max-age=300');
    if (!publisherId) return res.status(404).type('text/plain').send('app-ads.txt ainda não configurado.\n');
    return res.type('text/plain').send(`google.com, ${publisherId}, DIRECT, f08c47fec0942fa0\n`);
  });
  return router;
}
