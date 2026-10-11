# Plan de trabajo con Git (IE9)

La idea es que el historial refleje el trabajo real de ambos: ir incorporando, ejecutando y revisando un servicio a la vez,
cada uno en su propia rama. Cada integrante hace sus propios commits desde su cuenta.

## Ramas

- `main`: versión estable (solo recibe merges desde `develop`).
- `develop`: integración.
- `feature/<tema>`: una por funcionalidad, que se une a `develop` con pull request.

## Orden sugerido

| Rama | Contenido | Ejemplos de commit |
|---|---|---|
| `feature/base-proyecto` | README, `.gitignore`, `docker-compose.yml`, `docker/init-db.sql`, scripts | `chore: agregar docker-compose con las 4 bases de datos` |
| `feature/auth-service` | modelo, repositorios, JWT y login; luego CRUD de usuarios y comercios | `feat(auth): emitir JWT con rol y comercioId`, `test(auth): pruebas de login` |
| `feature/inventario-service` | catálogo y stock; luego reservas | `feat(inventario): reservar stock todo o nada`, `fix(inventario): ordenar SKUs al bloquear` |
| `feature/ordenes-service` | órdenes y estados; luego webhook idempotente y cliente de Inventario | `feat(ordenes): evitar órdenes duplicadas por externalId` |
| `feature/picking-service` | picking, despacho y tracking | `feat(picking): generar tracking al despachar` |
| `feature/postman-y-docs` | colección de Postman, guion y guía de estudio | `docs: agregar colección de Postman con casos de error` |

## Recomendaciones

- Hagan commits pequeños y frecuentes: uno por clase o por cambio con sentido, no uno gigante por servicio.
- Cuando algo falle al compilar o probar, corríjanlo en un commit aparte (`fix: ...`): también cuenta como trazabilidad.
- Repartan los servicios entre los dos y revisen el pull request del otro antes de unirlo a `develop`.
- Al terminar, unan `develop` a `main` y compartan los enlaces en AVA.
