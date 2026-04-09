#!/bin/bash
# ==============================================
# LoveAll - Script de Deploy para VPS (Ubuntu)
# ==============================================
# Uso: scp deploy-vps.sh user@seu-servidor:~ && ssh user@seu-servidor "bash deploy-vps.sh"

set -e

APP_DIR="/opt/loveall"
REPO_URL="${REPO_URL:-}"  # Defina a URL do seu repo git

echo "=========================================="
echo " LoveAll - Deploy para VPS"
echo "=========================================="

# 1. Instalar dependencias do sistema
echo "[1/7] Instalando dependencias do sistema..."
sudo apt-get update -qq
sudo apt-get install -y -qq nodejs npm git build-essential python3

# Verificar Node.js 18+
NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 18 ]; then
  echo "Node.js 18+ necessario. Instalando via NodeSource..."
  curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
  sudo apt-get install -y nodejs
fi

# 2. Clonar ou atualizar o repositorio
echo "[2/7] Configurando diretorio do app..."
if [ -d "$APP_DIR" ]; then
  cd "$APP_DIR"
  if [ -n "$REPO_URL" ]; then
    git pull origin main
  fi
else
  if [ -n "$REPO_URL" ]; then
    sudo git clone "$REPO_URL" "$APP_DIR"
  else
    sudo mkdir -p "$APP_DIR"
    echo "Copie os arquivos do projeto para $APP_DIR"
  fi
  cd "$APP_DIR"
fi

sudo chown -R $USER:$USER "$APP_DIR"

# 3. Instalar dependencias
echo "[3/7] Instalando dependencias..."
cd "$APP_DIR/client" && npm ci
cd "$APP_DIR/server" && npm ci --omit=dev

# 4. Build do frontend
echo "[4/7] Build do frontend..."
cd "$APP_DIR/client" && npm run build

# 5. Configurar environment
echo "[5/7] Configurando variaveis de ambiente..."
if [ ! -f "$APP_DIR/server/.env" ]; then
  JWT_SECRET=$(openssl rand -hex 32)
  cat > "$APP_DIR/server/.env" << EOF
NODE_ENV=production
PORT=3001
JWT_SECRET=$JWT_SECRET
CORS_ORIGINS=https://seudominio.com
DB_PATH=./data/loveall.db
EOF
  echo "Arquivo .env criado com JWT_SECRET aleatorio."
  echo "IMPORTANTE: Edite $APP_DIR/server/.env e atualize CORS_ORIGINS com seu dominio."
fi

# 6. Seed do banco (se primeiro deploy)
echo "[6/7] Verificando banco de dados..."
mkdir -p "$APP_DIR/server/data"
if [ ! -f "$APP_DIR/server/data/loveall.db" ]; then
  cd "$APP_DIR/server" && node src/seed.js
  echo "Banco de dados criado com usuarios demo."
fi

# 7. Configurar systemd
echo "[7/7] Configurando servico systemd..."
sudo tee /etc/systemd/system/loveall.service > /dev/null << EOF
[Unit]
Description=LoveAll Dating App
After=network.target

[Service]
Type=simple
User=$USER
WorkingDirectory=$APP_DIR/server
ExecStart=/usr/bin/node src/index.js
Restart=on-failure
RestartSec=10
Environment=NODE_ENV=production

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable loveall
sudo systemctl restart loveall

echo ""
echo "=========================================="
echo " Deploy concluido!"
echo "=========================================="
echo " App rodando na porta 3001"
echo " Status: sudo systemctl status loveall"
echo " Logs:   sudo journalctl -u loveall -f"
echo ""
echo " Proximos passos:"
echo " 1. Edite $APP_DIR/server/.env com seu dominio"
echo " 2. Configure Nginx como proxy reverso (veja abaixo)"
echo " 3. Instale SSL com certbot"
echo "=========================================="
echo ""
echo " Exemplo de config Nginx (/etc/nginx/sites-available/loveall):"
echo ""
echo "  server {"
echo "      listen 80;"
echo "      server_name seudominio.com;"
echo ""
echo "      location / {"
echo "          proxy_pass http://localhost:3001;"
echo "          proxy_http_version 1.1;"
echo "          proxy_set_header Upgrade \$http_upgrade;"
echo "          proxy_set_header Connection 'upgrade';"
echo "          proxy_set_header Host \$host;"
echo "          proxy_set_header X-Real-IP \$remote_addr;"
echo "          proxy_cache_bypass \$http_upgrade;"
echo "      }"
echo "  }"
echo ""
echo "  Depois: sudo certbot --nginx -d seudominio.com"
echo "=========================================="
