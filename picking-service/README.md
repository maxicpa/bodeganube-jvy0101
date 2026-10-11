# picking-service - Picking y Despacho

Microservicio de BodegaNube (Spring Boot 3.3, Java 21, PostgreSQL).

- Puerto: **8084** - Base de datos: **picking_db** - Swagger UI: `http://localhost:8084/swagger-ui.html`

```bash
docker compose up -d      # PostgreSQL (desde la raíz del proyecto, ver docker-compose.yml)
mvn clean package         # compila, ejecuta pruebas y genera target/picking-service-1.0.0.jar
mvn spring-boot:run       # o: java -jar target/picking-service-1.0.0.jar
```

Endpoints, usuarios de prueba y variables de entorno: ver el `README.md` de la raíz del proyecto.
