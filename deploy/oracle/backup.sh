#!/usr/bin/env bash
set -euo pipefail
umask 077
cd -- "$(dirname -- "${BASH_SOURCE[0]}")"
compose=(docker compose --env-file .env -f compose.yaml)
container="$("${compose[@]}" ps -a -q api)"
[[ -n "$container" ]] || { echo 'A API ainda não possui contêiner para backup.' >&2; exit 1; }
volume="$(docker inspect --format '{{range .Mounts}}{{if eq .Destination "/data"}}{{.Name}}{{end}}{{end}}' "$container")"
image="$(docker inspect --format '{{.Image}}' "$container")"
[[ -n "$volume" ]] || { echo 'Volume persistente da API não encontrado.' >&2; exit 1; }
was_running="$(docker inspect --format '{{.State.Running}}' "$container")"
restart_api() {
  if [[ "$was_running" == true ]]; then "${compose[@]}" start api >/dev/null; fi
}
trap restart_api EXIT
if [[ "$was_running" == true ]]; then "${compose[@]}" stop --timeout 30 api; fi
mkdir -p backups
backup="backups/loveall-$(date -u +%Y%m%dT%H%M%SZ)-${RANDOM}.tar.gz"
# A pausa mantém SQLite, WAL e fotos no mesmo instante. O arquivo é escrito pelo usuário SSH.
docker run --rm --read-only --network none --volume "$volume:/data:ro" --entrypoint tar "$image" \
  -czf - -C /data . > "$backup.partial"
mv -- "$backup.partial" "$backup"
chmod 600 "$backup"
echo "Backup local criado: $backup"
echo 'Copie uma cópia protegida para fora da VM; o arquivo local sozinho não é recuperação de desastre.'
