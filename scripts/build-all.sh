#!/usr/bin/env bash
# Compila, ejecuta las pruebas y empaqueta los 4 microservicios (genera target/*.jar)
set -e
cd "$(dirname "$0")/.."
for d in auth-service inventario-service ordenes-service picking-service; do
  echo "=== $d ==="
  (cd "$d" && mvn clean package)
done
