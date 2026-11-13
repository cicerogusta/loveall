import 'dotenv/config';
import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import compression from 'compression';
import rateLimit from 'express-rate-limit';
import { createServer } from 'http';
import db from './database.js';
import { photoFilename } from './services/photo-store.js';
import { proxyHops } from './config/proxy.js';
import { createAppAdsRouter } from './routes/app-ads.js';

import authRoutes from './routes/auth.js';
import profileRoutes from './routes/profiles.js';
import likeRoutes from './routes/likes.js';
import matchRoutes from './routes/matches.js';
import messageRoutes from './routes/messages.js';
import safetyRoutes from './routes/safety.js';
import callRoutes from './routes/calls.js';
import billingRoutes from './routes/billing.js';
import photoRoutes from './routes/photos.js';
import { photoStore, maintainPhotos } from './services/photo-storage.js';

const isProduction = process.env.NODE_ENV === 'production';

const app = express();
app.set('trust proxy', proxyHops(process.env.TRUST_PROXY_HOPS));

const server = createServer(app);

// ===================== SEGURANCA =====================
app.use(
  helmet({
    contentSecurityPolicy: {
      directives: {
        defaultSrc: ["'self'"],
        scriptSrc: ["'self'", "'unsafe-inline'"],
        styleSrc: ["'self'", "'unsafe-inline'", 'https://fonts.googleapis.com'],
        fontSrc: ["'self'", 'https://fonts.gstatic.com'],
        imgSrc: ["'self'", 'data:', 'https:', 'blob:'],
        connectSrc: ["'self'", 'ws:', 'wss:'],
      },
    },
    crossOriginEmbedderPolicy: false,
  })
);

app.use(compression());

// CORS - em producao, restringir ao dominio
const allowedOrigins = process.env.CORS_ORIGINS
  ? process.env.CORS_ORIGINS.split(',')
  : ['http://localhost:5173', 'http://localhost:3001'];

app.use(
  cors({
    origin: isProduction ? allowedOrigins : '*',
    credentials: true,
  })
);

// Rate limit global - 100 req/min por IP
const globalLimiter = rateLimit({
  windowMs: 60 * 1000,
  max: 100,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: 'Muitas requisicoes. Tente novamente em 1 minuto.' },
});
app.use('/api/', globalLimiter);

// Rate limit mais restrito para auth
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 20,
  message: { error: 'Muitas tentativas de login. Tente novamente em 15 minutos.' },
});

app.use(express.json({ limit: '64kb' }));
app.use((req, res, next) => {
  if (req.is('application/json') && (!req.body || typeof req.body !== 'object' || Array.isArray(req.body))) {
    return res.status(400).json({ error: 'Envie um objeto JSON válido.' });
  }
  next();
});

// Log de requisições para debug
app.use((req, res, next) => {
  const originalJson = res.json;
  res.json = function (data) {
    console.log(`[${req.method}] ${req.path} -> ${res.statusCode}`);
    return originalJson.call(this, data);
  };
  next();
});

// ===================== API ROUTES =====================
app.use(createAppAdsRouter(process.env.ADMOB_PUBLISHER_ID || ''));
app.use('/api/auth', authLimiter, authRoutes);
app.use('/api/profiles', profileRoutes);
app.use('/api/profiles', photoRoutes);
app.get('/api/photos/:filename', async (req, res) => {
  const reference = `/api/photos/${req.params.filename}`;
  const filename = photoFilename(reference);
  if (!filename) return res.status(404).end();
  const visible = await db
    .prepare(
      `SELECT 1 FROM uploaded_photos p JOIN users u ON p.user_id = u.id
    WHERE p.reference = ? AND p.ready = 1 AND p.pending_delete = 0 AND u.is_active = 1 AND u.is_banned = 0`
    )
    .get(reference);
  if (!visible) return res.status(404).end();
  const bytes = await photoStore.get(filename);
  if (!bytes) return res.status(404).end();
  res
    .set({ 'Cache-Control': 'no-store', 'Content-Type': 'image/jpeg', 'X-Content-Type-Options': 'nosniff' })
    .send(bytes);
});
app.use('/api/likes', likeRoutes);
app.use('/api/matches', matchRoutes);
app.use('/api/messages', messageRoutes);
app.use('/api/safety', safetyRoutes);
app.use('/api/calls', callRoutes);
app.use('/api/billing', billingRoutes);

app.get('/api/health', async (req, res) => {
  try {
    await db.prepare('SELECT 1').get();
  } catch {
    return res.status(503).json({ status: 'unavailable' });
  }
  res.json({
    status: 'ok',
    app: 'LoveAll',
    version: '1.0.0',
    env: isProduction ? 'production' : 'development',
    uptime: Math.floor(process.uptime()),
  });
});

// O Android usa REST; os eventos Socket.IO do cliente legado não são expostos.
app.use((error, _req, res, _next) => {
  if (error.type === 'entity.too.large') return res.status(413).json({ error: 'Requisição muito grande.' });
  if (error instanceof SyntaxError && error.status === 400)
    return res.status(400).json({ error: 'JSON inválido.' });
  console.error('[API] Falha interna:', error.name);
  res.status(500).json({ error: 'Não foi possível concluir a operação.' });
});
await maintainPhotos();
const photoMaintenance = setInterval(
  () => {
    maintainPhotos().catch(() => console.error('[Photos] Manutenção indisponível.'));
  },
  60 * 60 * 1000
);
photoMaintenance.unref();

// ===================== START =====================
const PORT = process.env.PORT || 3001;
server.listen(PORT, '0.0.0.0', () => {
  console.log(`LoveAll Server [${isProduction ? 'PRODUCTION' : 'DEV'}] rodando na porta ${PORT}`);
});

let closing = false;
async function shutdown() {
  if (closing) return;
  closing = true;
  clearInterval(photoMaintenance);
  server.close(async () => {
    await db.close();
    process.exit(0);
  });
  setTimeout(() => process.exit(1), 30_000).unref();
}
process.on('SIGTERM', shutdown);
process.on('SIGINT', shutdown);
