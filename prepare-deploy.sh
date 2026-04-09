#!/bin/bash

echo "🚀 LoveAll Deploy Preparation"
echo "=============================="
echo ""

# Verificar se está na raiz do projeto
if [ ! -f "DEPLOY.md" ]; then
  echo "❌ Execute este script na raiz do projeto (E:/DEV/Projetos/ClaudeApps/LoveAll)"
  exit 1
fi

echo "✅ Verificando estrutura..."

# Backend
if [ -d "server" ] && [ -f "server/package.json" ]; then
  echo "✓ Backend pronto"
else
  echo "❌ Pasta 'server' não encontrada"
  exit 1
fi

# Frontend
if [ -d "client" ] && [ -f "client/package.json" ]; then
  echo "✓ Frontend pronto"
else
  echo "❌ Pasta 'client' não encontrada"
  exit 1
fi

echo ""
echo "📝 Checklist para Deploy:"
echo ""

read -p "1. Você criou um repositório GitHub e já fez push? (s/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Ss]$ ]]; then
  echo "✓ GitHub repo pronto"
else
  echo "⚠️  Você precisa fazer push para GitHub primeiro"
  echo "   git init"
  echo "   git add ."
  echo "   git commit -m 'Initial commit'"
  echo "   git remote add origin https://github.com/SEU_USER/loveall.git"
  echo "   git push -u origin main"
fi

read -p "2. Você tem credenciais de Mercado Pago? (s/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Ss]$ ]]; then
  echo "✓ Mercado Pago pronto"
else
  echo "⚠️  Crie uma conta em https://www.mercadopago.com.br (gratuita)"
fi

echo ""
echo "🎯 Próximos passos:"
echo ""
echo "1. Backend (Render):"
echo "   - Vá em https://render.com"
echo "   - Clique 'New' → 'Web Service'"
echo "   - Conecte seu GitHub"
echo "   - Root Directory: server"
echo "   - Start Command: node src/index.js"
echo ""
echo "2. Frontend (Vercel):"
echo "   - Vá em https://vercel.com"
echo "   - Clique 'Add New' → 'Project'"
echo "   - Selecione seu repositório"
echo "   - Root Directory: client"
echo "   - Build Command: npm run build"
echo ""
echo "3. Ler o guia completo:"
echo "   - cat DEPLOY.md"
echo ""
echo "✅ Preparação concluída!"
