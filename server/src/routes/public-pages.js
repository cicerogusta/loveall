import { Router, urlencoded } from 'express';
import rateLimit from 'express-rate-limit';
import bcrypt from 'bcryptjs';
import db from '../database.js';
import { deleteAccount } from '../services/account-deletion.js';
import { page, privacy, terms, childSafety, deletionForm } from '../public/pages.js';

export function createPublicPagesRouter(origin = process.env.PUBLIC_SITE_URL || 'https://loveall-api.onrender.com') {
  const allowedOrigin = new URL(origin).origin;
  const router = Router();
  const show = (res, title, body, status = 200) => res.status(status).set('Cache-Control', 'no-store').type('html').send(page(title, body));
  router.get('/', (_req, res) => show(res, 'LoveAll', '<section><p>Aplicativo Android de relacionamentos para maiores de 18 anos.</p><p>Consulte nossa política de privacidade, termos, padrões de segurança infantil ou exclua sua conta pelos links acima.</p></section>'));
  router.get('/privacy', (_req, res) => show(res, 'Política de privacidade', privacy));
  router.get('/terms', (_req, res) => show(res, 'Termos de uso', terms));
  router.get('/child-safety', (_req, res) => show(res, 'Padrões de segurança infantil', childSafety));
  router.get('/delete-account', (_req, res) => show(res, 'Excluir conta LoveAll', deletionForm()));
  const limiter = rateLimit({ windowMs: 15 * 60_000, limit: 6,
    message: 'Muitas tentativas. Aguarde 15 minutos antes de tentar novamente.' });
  router.post('/delete-account', limiter, urlencoded({ extended: false, limit: '8kb' }), async (req, res) => {
    // O formulário pede credenciais novamente e não usa cookies ou tokens na URL.
    // Rejeita submissões de outro site mesmo que uma pessoa mantenha a página aberta.
    if (req.get('origin') !== allowedOrigin) return show(res, 'Solicitação recusada', '<p>Abra a página de exclusão diretamente no site do LoveAll e tente novamente.</p>', 403);
    const { email, password, confirmation } = req.body || {};
    if (typeof email !== 'string' || email.length > 254 || typeof password !== 'string' || password.length < 1 || password.length > 128 || confirmation !== 'delete') {
      return show(res, 'Confira os dados', '<p>Preencha email, senha e a confirmação de exclusão.</p><a href="/delete-account">Voltar</a>', 400);
    }
    const user = await db.prepare('SELECT id, password FROM users WHERE email = ?').get(email.trim().toLowerCase());
    // O mesmo custo de hash para conta ausente reduz a diferença observável de tempo.
    const matches = await bcrypt.compare(password, user?.password || dummyHash);
    if (!user || !matches) return show(res, 'Não foi possível confirmar a conta', '<p>Email ou senha incorretos. Nenhum dado foi excluído.</p><a href="/delete-account">Tentar novamente</a>', 401);
    await deleteAccount(user.id);
    return show(res, 'Conta excluída', '<p>Sua conta LoveAll e os registros associados foram removidos da base ativa. Não é necessário reinstalar o aplicativo.</p>');
  });
  return router;
}
const dummyHash = bcrypt.hashSync('not-a-real-account-password', 12);
