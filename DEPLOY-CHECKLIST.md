# 📋 LoveAll Deploy Checklist

## Pre-Deploy (Local)

- [ ] Código compilado sem erros (`npm run build` em client)
- [ ] Backend roda localmente (`PORT=4000 node src/index.js` em server)
- [ ] Testes básicos funcionam:
  - [ ] Registrar usuário
  - [ ] Login
  - [ ] Ver planos
  - [ ] Simular pagamento (Mercado Pago sandbox)

## GitHub Setup

- [ ] Repositório criado em GitHub
- [ ] `.gitignore` presente (evita enviado `.env`, `node_modules`)
- [ ] `git push origin main` feito
- [ ] Branch `main` é padrão

## Render (Backend) Setup

### Account
- [ ] Conta criada em render.com
- [ ] GitHub conectado

### Web Service Configuration
- [ ] Nome: `loveall-api`
- [ ] GitHub repo conectado
- [ ] Root directory: `server`
- [ ] Build command: `npm install` (padrão)
- [ ] Start command: `node src/index.js`
- [ ] Plan: **Free** ✓

### Environment Variables (Copiar exatamente)
```
NODE_ENV=production
PORT=3000
JWT_SECRET=VALOR_ALEATORIO_SEGURO_AQUI
CORS_ORIGINS=https://loveall.vercel.app,http://localhost:5173
DB_PATH=/var/data/loveall.db

MERCADO_PAGO_ACCESS_TOKEN=APP_USR_xxxxx
MERCADO_PAGO_PUBLIC_KEY=APP_USR_PUBLIC_xxxxx
MERCADO_PAGO_WEBHOOK_TOKEN=VALOR_ALEATORIO_SEGURO_AQUI
FRONTEND_URL=https://loveall.vercel.app
MERCADO_PAGO_MODE=test
```

- [ ] Deploy iniciado
- [ ] Aguardar conclusão (5-10 minutos)
- [ ] API acessível em `https://loveall-api.onrender.com/api/health` (retorna JSON)

## Vercel (Frontend) Setup

### Account
- [ ] Conta criada em vercel.com
- [ ] GitHub conectado

### Project Configuration
- [ ] GitHub repo conectado
- [ ] Framework: **Vite**
- [ ] Root directory: `client`
- [ ] Build command: `npm run build`
- [ ] Output directory: `dist`

### Environment Variables
```
VITE_API_URL=https://loveall-api.onrender.com/api
```

- [ ] Deploy iniciado
- [ ] Aguardar conclusão (2-3 minutos)
- [ ] Frontend acessível em `https://loveall.vercel.app`

## Mercado Pago Setup

### Sandbox (Teste)
- [ ] Credenciais já configuradas em Render (modo teste)
- [ ] Cartão de teste: `4111 1111 1111 1111` / CVV `123`

### Webhook
- [ ] Vá em [Mercado Pago Dashboard](https://dashboard.mercadopago.com)
- [ ] Configurações → Webhooks
- [ ] URL: `https://loveall-api.onrender.com/api/calls/mercado-pago/webhook`
- [ ] Eventos: `payment`
- [ ] Teste webhook (deve retornar 200)

## Final Testing

### End-to-End Test
1. [ ] Acesse `https://loveall.vercel.app`
2. [ ] Registre novo usuário
3. [ ] Faça login
4. [ ] Navegue para "Planos"
5. [ ] Clique "Assinar" em qualquer plano
6. [ ] Seja redirecionado para Mercado Pago
7. [ ] Use cartão `4111 1111 1111 1111`
8. [ ] Confirme pagamento
9. [ ] Retorne ao app com plano ativado
10. [ ] Verifique minutos em "Meu Plano"

### Verificações Técnicas
- [ ] API health check: `curl https://loveall-api.onrender.com/api/health`
- [ ] Logs do Render mostram 200 OK
- [ ] Nenhum erro no console do navegador (F12)
- [ ] Banco de dados acessível (verificar logs)

## Production Readiness

### Security
- [ ] `JWT_SECRET` não é valor padrão
- [ ] `MERCADO_PAGO_WEBHOOK_TOKEN` não é valor padrão
- [ ] `.env` não foi commitado
- [ ] CORS configurado apenas para domínios conhecidos

### Monitoring
- [ ] Render alerts ativados
- [ ] Vercel analytics ativado
- [ ] Plano Mercado Pago entendido

### Backup
- [ ] Sabe onde está banco de dados (`/var/data/loveall.db` em Render)
- [ ] Entende que dados persistem entre redeploys

## Production Migration (Quando tiver usuários)

- [ ] Atualizar credenciais Mercado Pago para **produção**
- [ ] Mudar `MERCADO_PAGO_MODE=production`
- [ ] Aumentar limite de requests
- [ ] Considerar upgrade Render se traffic > 750h/mês

---

## 🚀 Status Atual

| Componente | Status | URL |
|-----------|--------|-----|
| Backend | ✅ Pronto | https://loveall-api.onrender.com |
| Frontend | ✅ Pronto | https://loveall.vercel.app |
| BD Dados | ✅ SQLite | /var/data/loveall.db |
| Pagamentos | ✅ Mercado Pago | Sandbox ativo |

---

## 📞 Troubleshooting Rápido

| Problema | Solução |
|----------|---------|
| "API não encontrada" | Verifica URL em FRONTEND_URL e Vercel env vars |
| "CORS error" | Atualiza CORS_ORIGINS em Render |
| "Pagamento não processa" | Verifica credenciais MP e webhook URL |
| "Frontend não carrega" | Verifica build logs em Vercel |
| "Backend lento" | Render free pode ter cold starts (15s primero request) |

---

**✅ Checklist completo? Seu app está 100% online em produção!**
