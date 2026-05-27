# Cally Mobile Auth Service

## Overview
Cally Mobile Auth Service is the authentication and authorization component of the "Cally" meeting and reminder voice assistant ecosystem. It handles user authentication, JWT management, Google OAuth2 integration, and provides secure communication with other services via RabbitMQ.

This service is part of a larger system that converts voice commands to text, processes them with AI (Gemini), and manages calendars and notifications.

## Tech Stack
- **Language:** Java 21
- **Framework:** Spring Boot 3.2.1
- **Security:** Spring Security, OAuth2 Client, JWT (jjwt)
- **Database:** MySQL 8.0 (JPA/Hibernate)
- **Messaging:** RabbitMQ (Spring AMQP)
- **Monitoring:** Micrometer, Prometheus, Grafana
- **Resilience:** Resilience4j (Circuit Breaker, Retry)
- **APIs:** OpenFeign, Google OAuth2
- **Documentation:** Swagger/OpenAPI (Jakarta)
- **Testing:** JUnit 5, Mockito, Testcontainers

## Requirements
- **JDK 21** or higher
- **Maven 3.8+**
- **Docker & Docker Compose**
- **MySQL 8.0** (if running locally without Docker)
- **RabbitMQ** (if running locally without Docker)

## Setup & Run

### Local Environment
1. Clone the repository.
2. Create/update the `proprts.env` file (or set environment variables) based on the [Environment Variables](#environment-variables) section.
3. Start the infrastructure (MySQL, Prometheus, Grafana) using Docker Compose:
   ```bash
   docker-compose up -d
   ```
4. Build the application:
   ```bash
   mvn clean install
   ```
5. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Docker
To build and run the entire stack:
```bash
docker-compose up --build
```

### Kubernetes (Minikube)
Deployment manifests are located in the `k8s/` directory.
Refer to `MinikubeBuildGuidance.txt` for specific instructions on deploying to Minikube.

## Environment Variables
The application uses the following environment variables (mapped in `src/main/resources/application.properties`):

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_URL` | MySQL Connection URL | `jdbc:mysql://localhost:3307/authservice...` |
| `DB_USERNAME` | Database username | `root` |
| `DB_PASSWORD` | Database password | `1111` |
| `SERVER_PORT` | Application port | `8081` |
| `JWT_SECRET` | Secret key for JWT signing | `SuperSecureKey...` |
| `JWT_EXPIRATION_MS` | JWT validity period (ms) | `86400000` |
| `RABBIT_HOST` | RabbitMQ host | `localhost` |
| `RABBIT_PORT` | RabbitMQ port | `5672` |
| `AUTH_QUEUE_NAME` | RabbitMQ queue name | `auth.cally.mobile.queue` |
| `SMTP_HOST` | SMTP server host | `smtp.gmail.com` |
| `SMTP_USERNAME` | SMTP username | `user@gmail.com` |
| `SMTP_PASSWORD` | SMTP app password | `xxxx xxxx xxxx xxxx` |
| `GOOGLE_CLIENT_ID` | Google OAuth2 Client ID | `...apps.googleusercontent.com` |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 Client Secret | `...` |

Note: A sample file `proprts.env` is provided in the root directory for reference.

## Scripts & Commands
- `mvn clean install`: Build the project and run tests.
- `mvn spring-boot:run`: Run the application locally.
- `docker-compose up -d`: Start backing services (MySQL, Prometheus, Grafana).
- `mvn test`: Execute unit and integration tests.

## Project Structure
- `src/main/java/com/example/project/`
  - `config/`: Security, RabbitMQ, and general configurations.
  - `rest/`: REST controllers.
  - `services/`: Business logic.
  - `repository/`: Data access layer.
  - `dto/`: Data Transfer Objects.
  - `entity/`: Database entities.
  - `logs/`: Logging aspects (AOP).
  - `metrics/`: Custom monitoring metrics.
- `k8s/`: Kubernetes manifests (Service, DB, Monitoring).
- `prometheus/`: Prometheus configuration.

## Tests
Unit and integration tests are located in `src/test/java`.
To run tests:
```bash
mvn test
```
The project uses **Mockito** for mocking and **Testcontainers** for RabbitMQ integration tests.

## Monitoring
- **Prometheus:** Accessible at `http://localhost:9090` (if using Docker Compose).
- **Grafana:** Accessible at `http://localhost:3000`. Use the `auth-service-grafana.json` file to import the dashboard.
- **Actuator:** Health and metrics endpoints are exposed at `/actuator`.

## TODO
- [ ] Add license information.
- [ ] Finalize Google OAuth2 redirect URIs for production.
- [ ] Complete SMS/Push notification service integration.

## License
TODO: Add license (e.g., MIT, Apache 2.0).
