import 'dotenv/config';
import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import compression from 'compression';
import rateLimit from 'express-rate-limit';
import { createServer } from 'http';
import { Server } from 'socket.io';
import jwt from 'jsonwebtoken';
import path from 'path';
import fs from 'fs';
import { fileURLToPath } from 'url';
import db from './database.js';

import authRoutes from './routes/auth.js';
import profileRoutes from './routes/profiles.js';
import likeRoutes from './routes/likes.js';
import matchRoutes from './routes/matches.js';
import messageRoutes from './routes/messages.js';
import safetyRoutes from './routes/safety.js';
import callRoutes from './routes/calls.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const isProduction = process.env.NODE_ENV === 'production';

const app = express();

const server = createServer(app);

// ===================== SEGURANCA =====================
app.use(helmet({
  contentSecurityPolicy: {
    directives: {
      defaultSrc: ["'self'"],
      scriptSrc: ["'self'", "'unsafe-inline'"],
      styleSrc: ["'self'", "'unsafe-inline'", "https://fonts.googleapis.com"],
      fontSrc: ["'self'", "https://fonts.gstatic.com"],
      imgSrc: ["'self'", "data:", "https:", "blob:"],
      connectSrc: ["'self'", "ws:", "wss:"],
    },
  },
  crossOriginEmbedderPolicy: false,
}));

app.use(compression());

// CORS - em producao, restringir ao dominio
const allowedOrigins = process.env.CORS_ORIGINS
  ? process.env.CORS_ORIGINS.split(',')
  : ['http://localhost:5173', 'http://localhost:3001'];

app.use(cors({
  origin: isProduction ? allowedOrigins : '*',
  credentials: true,
}));

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

app.use(express.json({ limit: '10mb' }));

// Log de requisições para debug
app.use((req, res, next) => {
  const originalJson = res.json;
  res.json = function(data) {
    console.log(`[${req.method}] ${req.path} -> ${res.statusCode}`);
    return originalJson.call(this, data);
  };
  next();
});

// ===================== SOCKET.IO =====================
const io = new Server(server, {
  cors: {
    origin: isProduction ? allowedOrigins : '*',
    methods: ['GET', 'POST'],
    credentials: true,
  }
});

// ===================== API ROUTES =====================
app.use('/api/auth', authLimiter, authRoutes);
app.use('/api/profiles', profileRoutes);
app.use('/api/likes', likeRoutes);
app.use('/api/matches', matchRoutes);
app.use('/api/messages', messageRoutes);
app.use('/api/safety', safetyRoutes);
app.use('/api/calls', callRoutes);

app.get('/api/health', (req, res) => {
  res.json({
    status: 'ok',
    app: 'LoveAll',
    version: '1.0.0',
    env: isProduction ? 'production' : 'development',
    uptime: Math.floor(process.uptime()),
  });
});

// ===================== SERVIR FRONTEND EM PRODUCAO =====================
const clientDist = path.resolve(__dirname, '..', '..', 'client', 'dist');
const indexHtml = path.join(clientDist, 'index.html');

if (fs.existsSync(clientDist)) {
  app.use(express.static(clientDist, {
    maxAge: isProduction ? '7d' : 0,
    etag: true,
  }));

  // SPA fallback - todas as rotas nao-API servem o index.html
  app.get('*', (req, res) => {
    if (req.path.startsWith('/api/') || req.path.startsWith('/socket.io/')) {
      return res.status(404).json({ error: 'Endpoint nao encontrado' });
    }
    res.sendFile(indexHtml);
  });
} else {
  console.log('Frontend dist nao encontrado em:', clientDist);
  console.log('Rode "cd client && npm run build" para gerar o frontend.');
}

// ===================== SOCKET.IO REALTIME =====================
const onlineUsers = new Map();

io.use((socket, next) => {
  const token = socket.handshake.auth.token;
  if (!token) return next(new Error('Token necessario'));

  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    socket.userId = decoded.id;
    next();
  } catch {
    next(new Error('Token invalido'));
  }
});

io.on('connection', (socket) => {
  onlineUsers.set(socket.userId, socket.id);

  socket.on('go-online', () => {
    const matches = db.prepare(`
      SELECT CASE WHEN user1 = ? THEN user2 ELSE user1 END as partner_id
      FROM matches WHERE (user1 = ? OR user2 = ?) AND is_active = 1
    `).all(socket.userId, socket.userId, socket.userId);

    matches.forEach(m => {
      const partnerSocket = onlineUsers.get(m.partner_id);
      if (partnerSocket) {
        io.to(partnerSocket).emit('user-online', { userId: socket.userId });
      }
    });
  });

  socket.on('send-message', (data) => {
    const receiverSocket = onlineUsers.get(data.receiverId);
    if (receiverSocket) {
      io.to(receiverSocket).emit('new-message', {
        matchId: data.matchId,
        senderId: socket.userId,
        content: data.content,
        msgType: data.msgType || 'text',
        createdAt: new Date().toISOString()
      });
    }
  });

  socket.on('new-match', (data) => {
    const receiverSocket = onlineUsers.get(data.partnerId);
    if (receiverSocket) {
      io.to(receiverSocket).emit('match-notification', {
        matchId: data.matchId,
        userId: socket.userId
      });
    }
  });

  socket.on('typing', (data) => {
    const receiverSocket = onlineUsers.get(data.receiverId);
    if (receiverSocket) {
      io.to(receiverSocket).emit('user-typing', {
        matchId: data.matchId,
        userId: socket.userId
      });
    }
  });

  socket.on('call-request', (data) => {
    const receiverSocket = onlineUsers.get(data.receiverId);
    if (receiverSocket) {
      io.to(receiverSocket).emit('incoming-call', {
        callerId: socket.userId,
        callerName: data.callerName,
        callType: data.callType,
        matchId: data.matchId
      });
    }
  });

  socket.on('call-response', (data) => {
    const callerSocket = onlineUsers.get(data.callerId);
    if (callerSocket) {
      io.to(callerSocket).emit('call-answered', {
        accepted: data.accepted,
        receiverId: socket.userId
      });
    }
  });

  socket.on('disconnect', () => {
    onlineUsers.delete(socket.userId);
  });
});

// ===================== START =====================
const PORT = process.env.PORT || 3001;
server.listen(PORT, '0.0.0.0', () => {
  console.log(`LoveAll Server [${isProduction ? 'PRODUCTION' : 'DEV'}] rodando na porta ${PORT}`);
});
