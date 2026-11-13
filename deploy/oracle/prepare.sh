#!/usr/bin/env bash
set -euo pipefail
umask 077
cd -- "$(dirname -- "${BASH_SOURCE[0]}")"

hostname="${1:-}"
if [[ ! "$hostname" =~ ^[a-z0-9]([a-z0-9.-]*[a-z0-9])?$ ]] || [[ "$hostname" != *.* ]] || [[ "$hostname" == *..* ]] || ((${#hostname} > 253)); then
  echo 'Uso: bash deploy/oracle/prepare.sh api.seudominio.com' >&2
  exit 1
fi
if [[ -e .env ]]; then
  echo 'O arquivo .env já existe. Segredo preservado; confira o hostname antes de publicar.'
  exit 1
fi
command -v openssl >/dev/null || { echo 'Instale openssl antes de continuar.' >&2; exit 1; }
mkdir -p .secrets
chmod 700 .secrets
secret="$(openssl rand -hex 48)"
printf 'API_HOST=%s\nJWT_SECRET=%s\n' "$hostname" "$secret" > .env
chmod 600 .env
unset secret
echo 'Configuração criada na máquina de destino; segredo não exibido.'
