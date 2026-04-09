# Deploy LoveAll - Guia Completo

## 🚀 Arquitetura de Deploy Gratuita

- **Backend**: Render (Node.js + SQLite)
- **Frontend**: Vercel (React + Vite)
- **Banco de dados**: SQLite (armazenado no Render)
- **Pagamentos**: Mercado Pago (sandbox → produção)

---

## 1️⃣ Deploy do Backend (Render)

### Pré-requisitos
- Conta GitHub (para conectar o repositório)
- Conta Render (render.com)

### Passos

1. **Fazer push do código para GitHub**
```bash
cd E:/DEV/Projetos/ClaudeApps/LoveAll
git init
git add .
git commit -m "Initial commit - LoveAll dating app with Mercado Pago"
git remote add origin https://github.com/SEU_USER/loveall.git
git push -u origin main
```

2. **Conectar Render**
   - Acesse [render.com](https://render.com)
   - Clique "New" → "Web Service"
   - Conecte seu repositório GitHub
   - Selecione branch `main`
   - Configure:
     - **Name**: `loveall-api`
     - **Build Command**: `npm install` (deixa padrão)
     - **Start Command**: `node src/index.js`
     - **Root Directory**: `server`
     - **Plan**: Free

3. **Configurar Variáveis de Ambiente**
   No Render, vá em "Environment":
   ```
   NODE_ENV=production
   PORT=3000
   JWT_SECRET=seu_jwt_secret_aleatorio_aqui
   MERCADO_PAGO_ACCESS_TOKEN=APP_USR_...
   MERCADO_PAGO_PUBLIC_KEY=APP_USR_PUBLIC_...
   MERCADO_PAGO_WEBHOOK_TOKEN=seu_webhook_secret_aqui
   FRONTEND_URL=https://loveall.vercel.app
   DB_PATH=/var/data/loveall.db
   CORS_ORIGINS=https://loveall.vercel.app,http://localhost:5173
   ```

4. **Aguardar Deploy**
   - Render automaticamente faz build e deploy
   - URL gerada: `https://loveall-api.onrender.com`

---

## 2️⃣ Deploy do Frontend (Vercel)

### Pré-requisitos
- Conta Vercel (vercel.com)
- Conta GitHub

### Passos

1. **Conectar Vercel**
   - Acesse [vercel.com](https://vercel.com)
   - Clique "Add New" → "Project"
   - Importe seu repositório GitHub
   - Selecione branch `main`

2. **Configurar Build**
   - **Framework Preset**: Vite
   - **Root Directory**: `client`
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`

3. **Variáveis de Ambiente**
   No Vercel, vá em "Settings" → "Environment Variables":
   ```
   VITE_API_URL=https://loveall-api.onrender.com/api
   ```

4. **Deploy**
   - Clique "Deploy"
   - Aguardar build (2-3 minutos)
   - URL gerada: `https://loveall.vercel.app`

---

## 3️⃣ Configurar Mercado Pago

### Sandbox (Desenvolvimento)
Credenciais já estão no `.env` para teste.

**Cartão de teste**:
- Número: `4111 1111 1111 1111`
- CVV: `123`
- Data: Qualquer data futura

### Produção (Quando tem receita)
1. Vá em [dashboard.mercadopago.com](https://dashboard.mercadopago.com)
2. Em "Configurações" → "Integrações" → "Credenciais de Produção"
3. Copie `Access Token` e `Public Key`
4. Atualize em Render:
   - `MERCADO_PAGO_ACCESS_TOKEN` (produção)
   - `MERCADO_PAGO_MODE=production`

---

## 4️⃣ Configurar Webhook (Mercado Pago)

Para receber confirmações de pagamento:

1. No Mercado Pago Dashboard:
   - Vá em "Configurações" → "Webhooks"
   - **URL**: `https://loveall-api.onrender.com/api/calls/mercado-pago/webhook`
   - **Eventos**: Selecione `payment`

2. Teste webhook:
   - Clique "Enviar Teste"
   - Deve receber `200 OK`

---

## 5️⃣ Monitorar em Produção

### Render
- Dashboard: Logs em tempo real
- Se não carregar, verifique:
  - Variáveis de ambiente
  - Node version compatível
  - Permissões de arquivo `/var/data/`

### Vercel
- Analytics: Traffic, performance
- Logs de build: Se houver erro

### Aplicação
- Abra em `https://loveall.vercel.app`
- Teste fluxo completo:
  1. Registrar usuário
  2. Entrar
  3. Ir em "Planos"
  4. Assinar plano (será redirecionado para Mercado Pago)
  5. Usar cartão de teste
  6. Confirmar pagamento

---

## 💰 Limites Gratuitos

| Serviço | Limite |
|---------|--------|
| **Render** | 750 horas/mês (sempre ativo se único serviço) |
| **Vercel** | 100 deployments/mês, bandwidth ilimitado |
| **Mercado Pago** | Sem limite de transações |

---

## 🚨 Problemas Comuns

### "API não encontrada"
- Verifica se `FRONTEND_URL` em Render está correto
- Verifica CORS em `server/src/index.js`

### "Banco de dados vazio"
- Render cria `/var/data/` automaticamente
- Primeira requisição cria tabelas
- Dados persistem entre redeploys

### "Webhook não recebe pagamentos"
- Verifica se URL está exatamente: `https://loveall-api.onrender.com/api/calls/mercado-pago/webhook`
- Testa webhook manualmente no dashboard MP

### "Frontend lento"
- Vercel é rápido, issue pode ser API
- Render free pode ter cold starts
- Considerar upgrade se tiver usuários

---

## 📈 Próximos Passos (Monetização)

1. **Quando tiver 100+ usuários**:
   - Upgrade Render para plano pago ($7/mês)
   - Migrar SQLite para PostgreSQL

2. **Quando tiver $200+/mês em receita**:
   - Considerar VPS próprio (DigitalOcean $5/mês)
   - Integrar CDN (Cloudflare free)

3. **Escalabilidade futura**:
   - AWS/Google Cloud (vem com crédito gratuito)
   - Load balancer se tiver picos
   - Cache Redis

---

## 🔒 Segurança em Produção

- [ ] Mudar `JWT_SECRET` para valor aleatório
- [ ] Mudar `MERCADO_PAGO_WEBHOOK_TOKEN` para valor aleatório
- [ ] Verificar logs regularmente em Render
- [ ] Fazer backup do banco de dados (`/var/data/loveall.db`)
- [ ] Monitorar uso de banda em Vercel

---

**Questões? Acesse:**
- Render Docs: https://render.com/docs
- Vercel Docs: https://vercel.com/docs
- Mercado Pago API: https://developers.mercadopago.com/pt-br
