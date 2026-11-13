#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")"
[[ -f .env ]] || { echo 'Execute prepare.sh com o hostname da API.' >&2; exit 1; }
docker compose version >/dev/null
compose=(docker compose --env-file .env -f compose.yaml)
"${compose[@]}" config --quiet
"${compose[@]}" build --pull api
"${compose[@]}" pull caddy
if [[ -n "$("${compose[@]}" ps -a -q api)" ]]; then
  bash backup.sh
fi
"${compose[@]}" up -d --wait --wait-timeout 180
echo 'Contêineres iniciados. Valide o HTTPS público antes de configurar o Android.'
"${compose[@]}" ps
