#!/usr/bin/env bash
# Levanta los 4 servicios en segundo plano usando los .jar (ejecutar antes ./scripts/build-all.sh)
# y la base de datos (docker compose up -d). Los logs quedan en la carpeta logs/.
cd "$(dirname "$0")/.."
mkdir -p logs
for d in auth-service inventario-service ordenes-service picking-service; do
  jar=$(ls "$d"/target/*.jar 2>/dev/null | head -n 1)
  if [ -z "$jar" ]; then
    echo "No existe el .jar de $d. Ejecuta primero ./scripts/build-all.sh"
    exit 1
  fi
  nohup java -jar "$jar" > "logs/$d.log" 2>&1 &
  echo $! > "logs/$d.pid"
  echo "$d iniciado (PID $(cat logs/$d.pid)). Log: logs/$d.log"
  sleep 8
done
echo "Listo. Swagger: http://localhost:8081/swagger-ui.html (y 8082, 8083, 8084)"
