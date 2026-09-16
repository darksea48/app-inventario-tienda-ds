#!/usr/bin/env bash
# deploy/monitorear-salud.sh
#
# Consulta /actuator/health del color actualmente activo cada 10 segundos
# durante la ventana indicada. Si CUALQUIER consulta falla, termina con
# código distinto de 0 -- eso es lo que cd.yml usa para decidir el rollback
# automático (Actividad 3, §4.4 del informe).
#
# Uso:
#   ./monitorear-salud.sh --minutos 2 [--url http://localhost:8080/actuator/health]
#
set -euo pipefail

MINUTOS=2
URL="http://localhost:8080/actuator/health"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --minutos) MINUTOS="$2"; shift 2 ;;
    --url) URL="$2"; shift 2 ;;
    *) echo "Argumento desconocido: $1" >&2; exit 1 ;;
  esac
done

FIN=$(( $(date +%s) + MINUTOS * 60 ))
INTENTO=0

echo "Monitoreando $URL durante ${MINUTOS} minuto(s)..."

while [[ $(date +%s) -lt $FIN ]]; do
  INTENTO=$((INTENTO + 1))
  ESTADO=$(curl -s -o /dev/null -w '%{http_code}' "$URL" || echo "000")

  if [[ "$ESTADO" != "200" ]]; then
    echo "❌ Intento $INTENTO: $URL respondió $ESTADO (esperado 200)"
    exit 1
  fi

  echo "✅ Intento $INTENTO: $URL respondió 200"
  sleep 10
done

echo "Monitoreo completo: salud estable durante ${MINUTOS} minuto(s)."
