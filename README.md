# Resource Booking System

A RESTful Resource Booking System built with Spring Boot 3, Java 17, Spring Security 6, JWT, and MySQL.

## Tech Stack

| Technology | Version |
|---|---|
| Java | 17+ |
| Spring Boot | 3.2.5 |
| Spring Security | 6.x |
| JWT (jjwt) | 0.12.5 |
| MySQL | 8.x |
| Hibernate/JPA | 6.x |
| Swagger/OpenAPI | springdoc 2.5 |
| Lombok | Latest |
| H2 (tests) | Latest |

## Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.x running locally (or Docker)

## Database Setup

### Option 1: Local MySQL

```sql
CREATE DATABASE booking_db;
```

### Option 2: Docker

```bash
docker run -d --name mysql-booking \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=booking_db \
  -p 3306:3306 \
  mysql:8
```

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `booking_db` | Database name |
| `DB_USER` | `root` | Database username |
| `DB_PASSWORD` | `root` | Database password |
| `JWT_SECRET` | (built-in) | Base64-encoded 256-bit HMAC key |
| `JWT_EXPIRATION` | `86400000` | Token expiration in milliseconds (24h) |
| `SERVER_PORT` | `8080` | Application port |

## Build & Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# Run with custom env vars
DB_HOST=localhost DB_PASSWORD=mypassword mvn spring-boot:run
```

## Run Tests

```bash
mvn test
```

Tests use an embedded H2 database — no MySQL required.

## Seed Data

By default, in non-production environments (when `app.seed.enabled=true`), the application automatically seeds sample resources and roles. Do not use default credentials in production; they have been removed from the default properties for security.

## API Documentation

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

OpenAPI JSON: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

### Authentication

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/auth/login` | Public | Login and get JWT token |

### Resources

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/resources` | USER, ADMIN | List all resources (paginated) |
| GET | `/api/resources/{id}` | USER, ADMIN | Get resource by ID |
| POST | `/api/resources` | ADMIN | Create resource |
| PUT | `/api/resources/{id}` | ADMIN | Update resource |
| DELETE | `/api/resources/{id}` | ADMIN | Delete resource |

### Reservations

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/reservations` | USER (own), ADMIN (all) | List reservations (filtered, paginated) |
| GET | `/api/reservations/{id}` | USER (own), ADMIN | Get reservation by ID |
| POST | `/api/reservations` | USER, ADMIN | Create reservation (user from JWT) |
| PUT | `/api/reservations/{id}` | USER (own), ADMIN | Update reservation |
| DELETE | `/api/reservations/{id}` | ADMIN | Delete reservation |

**Query parameters for `GET /api/reservations`:**

| Parameter | Type | Description |
|---|---|---|
| `status` | String | Filter by `PENDING`, `CONFIRMED`, `CANCELLED` |
| `minPrice` | Decimal | Filter by minimum price |
| `maxPrice` | Decimal | Filter by maximum price |
| `page` | Integer | Page number (default: 0) |
| `size` | Integer | Page size (default: 10) |
| `sort` | String | Sort field and direction, e.g., `price,asc` or `startTime,desc` |

## Usage Example

### 1. Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "your_secure_password"}'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "username": "admin",
  "role": "ADMIN"
}
```

### 2. Create a Resource (ADMIN)

```bash
curl -X POST http://localhost:8080/api/resources \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name": "Meeting Room B", "description": "Small room", "type": "ROOM", "available": true}'
```

### 3. Create a Reservation (USER)

```bash
curl -X POST http://localhost:8080/api/reservations \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "resourceId": 1,
    "startTime": "2026-10-01T09:00:00",
    "endTime": "2026-10-01T11:00:00",
    "price": 150.00
  }'
```

### 4. List Reservations with Filters

```bash
curl "http://localhost:8080/api/reservations?status=PENDING&minPrice=100&maxPrice=500&page=0&size=5&sort=price,asc" \
  -H "Authorization: Bearer <token>"
```

## Project Structure

```
src/main/java/com/booking/
├── BookingApplication.java
├── config/          # DataSeeder, OpenApiConfig
├── controller/      # AuthController, ResourceController, ReservationController
├── dto/
│   ├── request/     # LoginRequest, ResourceRequest, ReservationRequest
│   └── response/    # AuthResponse, ResourceResponse, ReservationResponse, ApiErrorResponse
├── exception/       # GlobalExceptionHandler, custom exceptions
├── model/           # User, Resource, Reservation, Role, ReservationStatus
├── repository/      # JPA repositories
├── security/        # JWT filter, provider, SecurityConfig, entry points
├── service/         # AuthService, ResourceService, ReservationService
└── specification/   # ReservationSpecification (JPA Criteria)
```

## License

MIT
