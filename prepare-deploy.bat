@echo off
echo 🚀 LoveAll Deploy Preparation
echo ==============================
echo.

if not exist "DEPLOY.md" (
  echo ❌ Execute este script na raiz do projeto
  echo E:/DEV/Projetos/ClaudeApps/LoveAll
  pause
  exit /b 1
)

echo ✅ Verificando estrutura...

if exist "server\package.json" (
  echo ✓ Backend pronto
) else (
  echo ❌ Pasta 'server' não encontrada
  pause
  exit /b 1
)

if exist "client\package.json" (
  echo ✓ Frontend pronto
) else (
  echo ❌ Pasta 'client' não encontrada
  pause
  exit /b 1
)

echo.
echo 📝 Instruções de Deploy:
echo.
echo 1. GITHUB:
echo    git init
echo    git add .
echo    git commit -m "Initial commit - LoveAll"
echo    git remote add origin https://github.com/SEU_USER/loveall.git
echo    git push -u origin main
echo.
echo 2. RENDER (Backend):
echo    - Vá em https://render.com
echo    - Clique 'New' ^> 'Web Service'
echo    - Conecte seu GitHub
echo    - Root Directory: server
echo    - Start Command: node src/index.js
echo.
echo 3. VERCEL (Frontend):
echo    - Vá em https://vercel.com
echo    - Clique 'Add New' ^> 'Project'
echo    - Root Directory: client
echo    - Build Command: npm run build
echo.
echo 4. MERCADO PAGO:
echo    - Configure credenciais em Render
echo    - Adicione webhook em MP Dashboard
echo.
echo ✅ Leia DEPLOY.md para instruções completas
echo.
pause
