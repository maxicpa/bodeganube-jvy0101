# Guía de estudio: entender el proyecto antes de presentarlo

El video se evalúa explicando el código en vivo, así que conviene dominarlo. Esta guía resume lo esencial.
Lo mejor es leerla con el proyecto abierto y ejecutar el flujo completo al menos una vez.

## La idea en cinco líneas

1. Una venta llega por webhook a **Órdenes**.
2. Órdenes pide a **Inventario** reservar el stock.
3. Con stock, la orden queda lista para picking; sin stock, queda `SIN_STOCK`.
4. El operario hace el picking y despacha en **Picking y Despacho**, que genera el tracking.
5. **Autenticación** emite el JWT con el rol; los otros servicios lo validan.

## Qué hace cada servicio (y dónde mirar)

| Servicio | Entidades | Algo para destacar |
|---|---|---|
| auth | `Comercio` 1–N `Usuario` | `JwtService` firma el token; `AuthService.login` valida la contraseña con BCrypt |
| inventario | `Producto`; `Reserva` 1–N `ReservaItem` N–1 `Producto` | `ReservaService.reservar`: todo o nada, idempotente por `ordenId`, con bloqueo pesimista |
| ordenes | `Orden` 1–N `OrdenItem`; `AvisoWebhook` | `WebhookService` + `ProcesadorAvisos`: responde 202 y procesa en segundo plano |
| picking | `Despacho` 1–N `DespachoItem` | `DespachoService.despachar`: tracking, confirmar stock y marcar la orden |

## Decisiones de diseño (por qué se hizo así)

- **Una base de datos por microservicio.** Cada servicio es dueño de sus datos. Por eso las relaciones JPA
  (`@OneToMany`, `@ManyToOne`) existen solo dentro de cada servicio; entre servicios se referencian por id
  (`ordenId`, `comercioId`) y se consultan por REST.
- **Idempotencia con `externalId` único.** Si el canal reenvía el mismo aviso, no se crea otra orden: lo frena
  la tabla de avisos y la restricción única en la orden.
- **Respuesta rápida al webhook.** Responde 202 y deja el trabajo a `@Async`. Representa el papel de SQS + Lambda
  del diseño de la EP1; aquí se simplifica a un hilo en segundo plano.
- **Reserva de stock concurrente.** Bloqueo pesimista al reservar (con SKUs ordenados para evitar deadlocks) y
  `@Version` como protección extra.
- **Sin transacciones abiertas durante llamadas HTTP.** `OrdenService.crear` y `DespachoService` no son
  `@Transactional` a propósito.
- **Endpoints `/internal`.** Los usan solo los otros servicios y exigen la cabecera `X-Internal-Key`.
  En el diseño completo estarían detrás de API Gateway, sin exponerse a Internet.
- **DTOs como `record` y validaciones con Bean Validation.** La API no expone entidades.
- **Errores en `ProblemDetail`.** Un solo `GlobalExceptionHandler` por servicio.

## Limitaciones (conviene decirlas con honestidad)

- Los secretos por defecto son de desarrollo; en un despliegue real irían en variables de entorno o un gestor de secretos.
- `ddl-auto=update` es cómodo para el curso; en producción se usarían migraciones (Flyway o Liquibase).
- No hay Circuit Breaker, colas reales ni API Gateway: están en el diseño, pero fuera del alcance de esta evaluación.
- Si Inventario cae, la orden queda `PENDIENTE_RESERVA` y el operario puede usar `reintentar-reserva`.

## Preguntas que podrían hacerles

- **¿Por qué `SIN_STOCK` no se cancela?** Porque se puede reponer inventario y reintentar la reserva sin perder la venta.
- **¿Qué pasa si dos webhooks iguales llegan a la vez?** La restricción única de `externalId` hace que solo uno se procese.
- **¿Cómo sabe un comercio que no puede ver órdenes ajenas?** El token trae `comercioId` y `OrdenService.obtener` filtra por él; si no es suya, responde 404.
- **¿Qué hace `@PreAuthorize("hasRole('OPERARIO')")`?** Restringe el endpoint al rol del claim `rol` del token.
- **¿Para qué sirve el `pom.xml` de cada servicio?** Declara dependencias (web, JPA, PostgreSQL, security, validation, springdoc, test) y el plugin de Spring Boot que genera el `.jar`.

## Antes de grabar

- Corre el flujo completo en Postman y revisa los datos en la base.
- Ejecuta `mvn clean package` sin errores en los 4 servicios.
- Clona el repositorio en una carpeta nueva y sigue el README al pie de la letra.
- Reparte quién explica cada parte; ambos deben poder responder sobre cualquier servicio.
