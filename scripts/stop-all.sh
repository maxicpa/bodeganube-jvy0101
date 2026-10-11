#!/usr/bin/env bash
# Detiene los servicios iniciados con run-all.sh
cd "$(dirname "$0")/.."
for d in auth-service inventario-service ordenes-service picking-service; do
  if [ -f "logs/$d.pid" ]; then
    kill "$(cat "logs/$d.pid")" 2>/dev/null && echo "$d detenido"
    rm -f "logs/$d.pid"
  fi
done
