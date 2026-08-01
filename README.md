# Reservas API

API REST para la gestión de reservas de canchas deportivas (fútbol, tenis, pádel), desarrollada con Spring Boot. Permite a los usuarios explorar recintos y canchas, consultar disponibilidad horaria en tiempo real, y reservar sin riesgo de solapamiento de horarios, incluso bajo condiciones de concurrencia.

Este proyecto fue desarrollado como pieza de portfolio técnico, con énfasis en la aplicación de buenas prácticas de arquitectura, seguridad y manejo de concurrencia.

## Tabla de contenidos

- [Características](#características)
- [Stack tecnológico](#stack-tecnológico)
- [Arquitectura](#arquitectura)
- [Desafío técnico central](#desafío-técnico-central)
- [Instalación y ejecución](#instalación-y-ejecución)
- [Documentación de la API](#documentación-de-la-api)
- [Interfaz de pruebas](#interfaz-de-pruebas)
- [Decisiones técnicas](#decisiones-técnicas)
- [Mejoras pendientes](#mejoras-pendientes)
- [Licencia](#licencia)

## Características

- Autenticación y autorización mediante JWT, con soporte de roles (`USER`, `ADMIN`)
- Gestión de recintos (venues) y canchas, con sus respectivos horarios y tarifas
- Consulta de disponibilidad horaria por cancha y fecha
- Creación de reservas con validación estricta contra solapamiento de horarios
- Prevención de condiciones de carrera: dos usuarios no pueden reservar el mismo horario de forma simultánea, incluso si ambas solicitudes llegan al mismo tiempo
- Cancelación de reservas, restringida al propietario de la reserva o a un administrador
- Manejo centralizado de errores, con respuestas estructuradas y códigos de estado HTTP apropiados
- Migraciones de base de datos versionadas mediante Flyway
- Panel de pruebas en HTML y JavaScript, independiente de herramientas externas, para validar el funcionamiento de la API

## Stack tecnológico

| Categoría | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.x |
| Seguridad | Spring Security, JWT (jjwt) |
| Persistencia | Spring Data JPA / Hibernate |
| Base de datos | PostgreSQL 16 |
| Migraciones | Flyway |
| Gestión de dependencias | Maven |
| Contenedores | Docker, Docker Compose |
| Utilidades | Lombok, Jakarta Validation |

## Arquitectura

El proyecto sigue una arquitectura en capas, con separación explícita de responsabilidades:

```
com.tunombre.reservasapi
├── controller/     Expone los endpoints REST y delega la lógica en los services
├── service/        Contiene la lógica de negocio, validaciones y control transaccional
├── repository/     Acceso a datos mediante Spring Data JPA
├── entity/         Modelo de dominio persistente
├── dto/            Contratos de entrada y salida de la API (request/response)
├── security/       Configuración de Spring Security y manejo de JWT
└── exception/      Excepciones personalizadas y manejo centralizado de errores
```

Las entidades JPA no se exponen directamente en la API; se utilizan DTOs en su lugar. Esto desacopla el contrato público de los detalles de persistencia y evita problemas de serialización asociados a relaciones de carga diferida (lazy loading).

### Modelo de dominio

```
User (id, name, email, password, role)
   |
   | 1:N
   v
Reservation (id, user_id, court_id, reservation_date, start_time, end_time, status)
   ^
   | N:1
   |
Court (id, venue_id, name, sport, price_per_hour)
   ^
   | N:1
   |
Venue (id, name, address)
```

## Desafío técnico central

El aspecto más relevante del proyecto es garantizar que una misma cancha no pueda reservarse dos veces en un horario que se superpone, incluso bajo concurrencia real.

### Detección de solapamiento

Se resuelve mediante una consulta que compara intervalos de tiempo directamente en la base de datos:

```sql
WHERE r.court_id = :courtId
  AND r.reservation_date = :date
  AND r.status <> 'CANCELLED'
  AND r.start_time < :endTime
  AND r.end_time > :startTime
```

La condición `start_time < endTime AND end_time > startTime` cubre todos los casos posibles de solapamiento (parcial, total, o uno contenido dentro del otro) sin necesidad de reglas adicionales.

### Condiciones de carrera

Detectar el solapamiento no es suficiente si dos solicitudes llegan de forma simultánea: ambas transacciones podrían verificar que no existe conflicto antes de que la otra confirme su reserva, resultando en una duplicación del horario.

Para evitar este escenario, la creación de una reserva se ejecuta bajo el nivel de aislamiento `SERIALIZABLE`:

```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public ReservationResponse createReservation(...) { ... }
```

Este nivel de aislamiento obliga a que las transacciones concurrentes sobre el mismo recurso se comporten como si se ejecutaran de forma secuencial. La contrapartida es un mayor costo en bloqueos y posibles reintentos bajo alta concurrencia, una decisión razonable para el volumen esperado en este dominio, y que se reevaluaría con métricas reales en un entorno de producción.

## Instalación y ejecución

### Requisitos previos

- JDK 17 o superior
- Docker y Docker Compose
- Maven (opcionalmente, el wrapper `./mvnw` incluido en el proyecto)

### Pasos

1. Clonar el repositorio:

   ```bash
   git clone https://github.com/tu-usuario/reservas-api.git
   cd reservas-api
   ```

2. Levantar la base de datos:

   ```bash
   docker compose up -d
   ```

3. Ejecutar la aplicación:

   ```bash
   ./mvnw spring-boot:run
   ```

   Al iniciar, Flyway aplica automáticamente las migraciones correspondientes al esquema y a los datos iniciales.

4. Verificar que el servicio está disponible:

   ```bash
   curl http://localhost:8080/venues
   ```

La API queda disponible en `http://localhost:8080`.

### Configuración

Los siguientes valores se definen en `application.properties` (ver sección de [mejoras pendientes](#mejoras-pendientes) respecto a su externalización):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/reservas_db
spring.datasource.username=reservas_user
spring.datasource.password=reservas_pass
jwt.secret=<clave-de-al-menos-256-bits>
jwt.expiration-ms=86400000
```

## Documentación de la API

### Autenticación

| Método | Endpoint | Descripción | Autenticación |
|---|---|---|---|
| POST | `/auth/register` | Registra un nuevo usuario | No requerida |
| POST | `/auth/login` | Inicia sesión y devuelve un token JWT | No requerida |

### Recintos y canchas

| Método | Endpoint | Descripción | Autenticación |
|---|---|---|---|
| GET | `/venues` | Lista todos los recintos con sus canchas asociadas | Requerida |
| GET | `/venues/{id}` | Obtiene el detalle de un recinto | Requerida |
| GET | `/courts/{id}/availability?date=YYYY-MM-DD` | Devuelve la disponibilidad horaria de una cancha | Requerida |

### Reservas

| Método | Endpoint | Descripción | Autenticación |
|---|---|---|---|
| POST | `/reservations` | Crea una nueva reserva | Requerida |
| GET | `/reservations/me` | Lista las reservas del usuario autenticado | Requerida |
| DELETE | `/reservations/{id}` | Cancela una reserva (propietario o administrador) | Requerida |

### Ejemplo: creación de una reserva

```bash
curl -X POST http://localhost:8080/reservations \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "courtId": 1,
    "reservationDate": "2026-08-15",
    "startTime": "10:00:00",
    "endTime": "11:00:00"
  }'
```

Si el horario solicitado ya se encuentra ocupado, la API responde con estado `409 Conflict`:

```json
{
  "status": 409,
  "error": "Conflict",
  "messages": ["La cancha ya está reservada en ese horario"]
}
```

## Interfaz de pruebas

El repositorio incluye un panel de pruebas en `frontend-test/index.html`, desarrollado en HTML y JavaScript sin dependencias externas ni proceso de build, destinado a facilitar la interacción manual con la API.

Para utilizarlo:

1. Con la API en ejecución, abrir el archivo `frontend-test/index.html` directamente en el navegador.
2. Verificar la conexión con el servidor, registrar un usuario o iniciar sesión.
3. Explorar los recintos disponibles, consultar la disponibilidad horaria y crear una reserva seleccionando un horario libre.

Este panel constituye una herramienta de desarrollo y control de calidad; no representa la interfaz final del producto ni sigue un proceso de diseño de experiencia de usuario, dado que su propósito es exclusivamente funcional.

## Decisiones técnicas

- **Uso de DTOs en lugar de exponer entidades directamente**: desacopla el contrato de la API del modelo de persistencia y evita errores de serialización derivados de relaciones con carga diferida en Hibernate.
- **Flyway en lugar de `ddl-auto=update`**: el esquema de base de datos queda versionado y es reproducible en cualquier entorno, en lugar de depender de la inferencia automática de Hibernate.
- **`FetchType.LAZY` en las relaciones `@ManyToOne`**: evita la carga innecesaria de objetos completos por defecto, con impacto directo en el rendimiento.
- **Autenticación JWT sin estado**: no se gestionan sesiones del lado del servidor; cada solicitud se autentica de manera independiente mediante el token, conforme a los principios de una API REST.
- **Nivel de aislamiento `SERIALIZABLE` en la creación de reservas**: prioriza la consistencia de los datos por sobre el rendimiento en la operación crítica del dominio (ver sección [Desafío técnico central](#desafío-técnico-central)).

## Mejoras pendientes

Las siguientes mejoras han sido identificadas pero aún no implementadas. Se documentan de forma explícita como parte del alcance definido para esta versión del proyecto:

- Suite de pruebas unitarias (`ReservationService`, lógica de solapamiento) y de integración (`@SpringBootTest`)
- Documentación interactiva de la API mediante Swagger/OpenAPI
- Endpoints administrativos (`/admin/reservations`) para el rol `ADMIN`
- Externalización de credenciales y de la clave `jwt.secret` a variables de entorno (actualmente definidas en `application.properties` por simplicidad en el entorno de desarrollo local)
- Paginación en los listados de reservas
- Dockerfile para la aplicación (actualmente solo la base de datos se ejecuta en contenedor)
- Manejo específico de `AuthenticationException` para devolver el código `401` ante credenciales inválidas

## Licencia

Este proyecto se distribuye con fines educativos y de portfolio profesional.
