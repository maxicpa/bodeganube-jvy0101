# BodegaNube – Microservicios

Proyecto de la Evaluación Parcial 2 de JVY0101 (Java: Diseño y Construcción de Soluciones nativas en Nube, Duoc UC).

BodegaNube es una plataforma de bodega y despacho para comercios: recibe las ventas de un canal externo, reserva el stock, deja las órdenes listas para el picking y registra el despacho con un número de seguimiento. Está hecha con cuatro microservicios en Spring Boot, cada uno con su propia base de datos PostgreSQL.

| Microservicio | Puerto | Base de datos | Qué hace |
|---|---|---|---|
| `auth-service` | 8081 | `auth_db` | Login con JWT, usuarios (operario o comercio) y comercios |
| `inventario-service` | 8082 | `inventario_db` | Catálogo, stock y reservas por orden |
| `ordenes-service` | 8083 | `ordenes_db` | Webhook de ventas, órdenes y sus estados |
| `picking-service` | 8084 | `picking_db` | Picking, despacho y tracking |

## Cómo funciona

1. El canal externo llama al webhook de Órdenes (`POST /api/webhooks/ventas`). Órdenes responde 202 enseguida y procesa el aviso en segundo plano.
2. Órdenes crea la orden (si el `externalId` ya existe, no la duplica) y le pide a Inventario que reserve el stock.
   - Si hay stock, la orden queda `DISPONIBLE_PICKING`.
   - Si no hay, queda `SIN_STOCK` y no aparece para el picking.
3. El operario inicia el picking (`EN_PICKING`) y después despacha: se genera el tracking, se descuenta el stock y la orden pasa a `DESPACHADA`.
4. Cada comercio entra con su usuario y ve solo sus órdenes, con su estado y tracking.

Todos los servicios están organizados en capas: `controller`, `service`, `repository` y `model` (más `dto`, `exception` y `config`). El servicio de autenticación firma un JWT y los otros tres lo validan con la misma clave.

## Requisitos

- Java 21
- Maven 3.9 o superior
- Docker, para levantar PostgreSQL (o un PostgreSQL local con las 4 bases creadas)

## Cómo ejecutarlo

```bash
git clone <URL_DEL_REPOSITORIO>
cd <carpeta-del-repositorio>

# 1. Base de datos: crea auth_db, inventario_db, ordenes_db y picking_db
docker compose up -d

# 2. Compilar, ejecutar las pruebas y empaquetar los 4 servicios
./scripts/build-all.sh

# 3. Levantar cada servicio en una terminal distinta
# Ejecuta un comando por terminal, siempre desde la carpeta bodeganube:
mvn -f auth-service/pom.xml spring-boot:run
mvn -f inventario-service/pom.xml spring-boot:run
mvn -f ordenes-service/pom.xml spring-boot:run
mvn -f picking-service/pom.xml spring-boot:run
```

También se puede correr el `.jar` de cada servicio: `java -jar target/<servicio>-1.0.0.jar`.

Para levantar los 4 de una vez (después de compilar) hay dos scripts: `./scripts/run-all.sh` los deja corriendo en segundo plano (logs en `logs/`) y `./scripts/stop-all.sh` los detiene.
Cada servicio tiene su documentación en `http://localhost:<puerto>/swagger-ui.html`.

Si el puerto 5432 ya está ocupado, cambia el mapeo en `docker-compose.yml` (por ejemplo `5433:5432`) y arranca los servicios con `DB_URL=jdbc:postgresql://localhost:5433/<base>`.

## Usuarios y datos de prueba

Se crean solos la primera vez que arranca `auth-service` e `inventario-service`.

| Rol | Correo | Contraseña |
|---|---|---|
| Operario | `operario@bodeganube.cl` | `Operario123` |
| Comercio (Tienda Demo, id 1) | `tienda@demo.cl` | `Comercio123` |

Catálogo inicial: `SKU-001` (50 unidades), `SKU-002` (30), `SKU-003` (10) y `SKU-004` (5).

## Probar con Postman

Importa `postman/BodegaNube.postman_collection.json` y ejecuta las carpetas en orden: Autenticación, Inventario, Órdenes y Picking. La colección guarda sola los tokens y los ids, y cada request revisa el código HTTP esperado (200, 201, 202, 204, 400, 401, 403, 404 y 409).

Los webhooks se procesan en segundo plano, así que conviene esperar un segundo antes de consultar las órdenes.

## Endpoints

Todos, salvo los marcados como públicos, necesitan `Authorization: Bearer <token>` (se obtiene en `/api/auth/login`).

**auth-service**

| Método | Ruta | Quién |
|---|---|---|
| POST | `/api/auth/login` | público |
| GET | `/api/auth/me` | cualquier usuario autenticado |
| GET, POST, PUT, DELETE | `/api/comercios`, `/api/comercios/{id}` | operario |
| GET, POST, PUT, DELETE | `/api/usuarios`, `/api/usuarios/{id}` | operario |

**inventario-service**

| Método | Ruta | Quién |
|---|---|---|
| GET | `/api/productos`, `/api/productos/{id}` | cualquier usuario autenticado |
| POST, PUT, DELETE | `/api/productos`, `/api/productos/{id}` | operario |
| POST | `/api/productos/{id}/ajuste-stock` | operario |
| varios | `/internal/reservas/**` | solo otros servicios (`X-Internal-Key`) |

**ordenes-service**

| Método | Ruta | Quién |
|---|---|---|
| POST | `/api/webhooks/ventas` | canal externo (`X-Webhook-Token`), responde 202 |
| GET | `/api/avisos` | operario |
| GET | `/api/ordenes?estado=` | operario ve todas; comercio solo las suyas |
| GET | `/api/ordenes/{id}` | operario; comercio solo si es suya |
| GET | `/api/ordenes/picking` | operario |
| POST, PUT, DELETE | `/api/ordenes`, `/api/ordenes/{id}` | operario |
| POST | `/api/ordenes/{id}/reintentar-reserva` | operario |
| varios | `/internal/ordenes/**` | solo otros servicios |

**picking-service**

| Método | Ruta | Quién |
|---|---|---|
| POST | `/api/despachos` (body: `{"ordenId": 1}`) | operario |
| GET | `/api/despachos`, `/api/despachos/{id}`, `/api/despachos/orden/{ordenId}` | operario |
| PUT | `/api/despachos/{id}` | operario |
| POST | `/api/despachos/{id}/despachar` | operario |
| DELETE | `/api/despachos/{id}` | operario |

Los errores se devuelven en formato `ProblemDetail` (RFC 7807), con un mensaje que explica qué pasó.

## Variables de entorno (opcionales)

| Variable | Valor por defecto |
|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | `jdbc:postgresql://localhost:5432/<base>`, `bodega`, `bodega` |
| `JWT_SECRET` | clave de desarrollo (mínimo 32 caracteres; debe ser la misma en los 4 servicios) |
| `INTERNAL_KEY` | clave de las llamadas entre servicios (la misma en los 4) |
| `WEBHOOK_TOKEN` | `webhook-dev-token` |
| `INVENTARIO_URL`, `ORDENES_URL` | `http://localhost:8082`, `http://localhost:8083` |

Los valores por defecto son solo para desarrollo local.

## Pruebas

Cada servicio tiene pruebas unitarias con JUnit 5 y Mockito. Se ejecutan con `mvn test` (o dentro de `mvn package`).

## Trabajo con Git

- `main`: versión estable. `develop`: integración. `feature/<servicio>-<tema>`: una rama por funcionalidad.
- Mensajes de commit cortos y descriptivos, por ejemplo `feat(ordenes): evitar órdenes duplicadas por externalId`.
