#!/usr/bin/env bash
# deploy/blue-green-switch.sh
#
# Conmuta el tráfico del proxy (Nginx) entre los colores BLUE y GREEN, o
# revierte al color anterior (rollback). Ver Actividad 3, §4.3-§4.4 del
# informe.
#
# Uso:
#   ./blue-green-switch.sh --to green      # conmuta el proxy hacia GREEN
#   ./blue-green-switch.sh --to blue       # conmuta el proxy hacia BLUE
#   ./blue-green-switch.sh --rollback      # vuelve al color anterior
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONF="${SCRIPT_DIR}/nginx-activo.conf"

if [[ ! -f "$CONF" ]]; then
  echo "No se encontró $CONF" >&2
  exit 1
fi

ACTUAL=$(grep -oP 'proxy_pass http://app_\K(blue|green)' "$CONF" | head -n1)

if [[ "${1:-}" == "--rollback" ]]; then
  NUEVO=$([[ "$ACTUAL" == "blue" ]] && echo "green" || echo "blue")
  echo "🔴 ROLLBACK: revirtiendo tráfico de '$ACTUAL' a '$NUEVO'"
elif [[ "${1:-}" == "--to" ]]; then
  NUEVO="${2:?Debes indicar el color destino: blue|green}"
  if [[ "$NUEVO" != "blue" && "$NUEVO" != "green" ]]; then
    echo "Color inválido: $NUEVO (usa 'blue' o 'green')" >&2
    exit 1
  fi
  echo "🟢 Conmutando tráfico de '$ACTUAL' a '$NUEVO'"
else
  echo "Uso: $0 --to <blue|green> | --rollback" >&2
  exit 1
fi

if [[ "$ACTUAL" == "$NUEVO" ]]; then
  echo "El tráfico ya está en '$NUEVO'; no hay nada que conmutar."
  exit 0
fi

sed -i "s/app_${ACTUAL}/app_${NUEVO}/g" "$CONF"

# Recarga Nginx sin cortar conexiones en curso, si el contenedor está arriba.
if docker ps --format '{{.Names}}' | grep -qx proxy-nginx; then
  docker exec proxy-nginx nginx -s reload
fi

echo "Tráfico activo ahora en: $NUEVO"
