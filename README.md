# 🏆 Gaming Tournament Platform Backend

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-blue?logo=postgresql)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)](https://redis.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](https://opensource.org/licenses/MIT)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker)](https://www.docker.com/)

A robust, scalable, and production-ready backend system for managing competitive gaming tournaments. It provides everything from player registration and ELO-based matchmaking to secure Stripe payments and real-time leaderboards.

## ✨ Features

* **Tournament Lifecycle Management**: Create, manage, and track tournaments from registration to final results.
* **Intelligent Matchmaking**: ELO-based system for fair and competitive player matching.
* **Secure Authentication & Authorization**: JWT-based security with role-based access control (Admin/Player).
* **Integrated Payment Processing**: Seamless registration fees and payout handling via Stripe, including secure webhooks and refunds.
* **Real-time Leaderboards**: Dynamic rankings updated with player statistics and ELO scores.
* **Cloud-Native & Scalable**: Containerized with Docker and optimized for deployment on GCP Cloud Run.
* **Resilient Design**: Built with Resilience4j for fault tolerance and circuit breaker patterns.
* **Comprehensive Monitoring**: Health checks, metrics, and visualization with Spring Boot Actuator, Prometheus, and Grafana.

## 🏗️ System Architecture

The platform is built on a layered, microservice-ready architecture:

1. **API Layer**: REST controllers handling HTTP requests and responses.
2. **Service Layer**: Core business logic (tournaments, users, payments, matches).
3. **Repository Layer**: Data persistence using Spring Data JPA and Hibernate.
4. **Integration Layer**: External communication with Stripe and email services.
5. **Security Layer**: JWT validation, password encryption (BCrypt), and endpoint security.

### Key Services

| Service | Purpose |
| :--- | :--- |
| **AuthService** | User registration, login, and JWT token management. |
| **TournamentService** | Handles tournament creation, registration logic, and bracket generation. |
| **MatchService** | Manages match flow, result submission, and ELO calculation updates. |
| **PaymentService** | Integrates with Stripe for payment intents, webhooks, and refund processing. |
| **NotificationService** | Sends asynchronous email notifications for key events. |

## 🚀 Quick Start

### Prerequisites

Ensure you have the following installed on your local machine:
* **Java 17** or higher
* **Maven 3.9** or higher
* **PostgreSQL 14** or higher
* **Redis 7** or higher
* A **Stripe Account** for payment processing

### Installation & Local Development

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/tournament-platform-backend.git
   cd tournament-platform-backend
2. **Configure the Environment:**
    ```bash
   spring:
    datasource:
    url: jdbc:postgresql://localhost:5432/tournament_db
    username: your_db_username
    password: your_db_password
    jpa:
    hibernate:
    ddl-auto: validate # Recommended for production-like setups
    show-sql: true
    data:
    redis:
    host: localhost
    port: 6379
    mail:
    host: your-smtp-host
    port: 587
    username: your-smtp-username
    password: your-smtp-password
    
    stripe:
    secret-key: sk_test_your_stripe_secret_key
    webhook-secret: whsec_your_webhook_secret
    
    logging:
    level:
    com.yourpackage: DEBUG
   
3. Run Database Migrations (if using Flyway in production mode):
    ```bash
        mvn flyway:migrate
4. Start the Application:
    ```bash
        mvn spring-boot:run/ docker-compose up --build
   
5.Verify the Setup:
* The API will be available at: http://localhost:8080/api
* Access the interactive Swagger UI at: http://localhost:8080/swagger-ui/index.html

📚 API Usage
Once running, you can interact with the API using tools like Postman or directly via the integrated Swagger UI.
### Example Core Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|-----------|-------------|---------------|
| `POST` | `/api/auth/register` | Register a new user | No |
| `POST` | `/api/auth/login` | Authenticate and receive a JWT | No |
| `GET` | `/api/tournaments` | List all available tournaments | No |
| `POST` | `/api/tournaments` | Create a new tournament | **Admin** |
| `POST` | `/api/tournaments/{id}/register` | Register for a tournament | **Player** |
| `POST` | `/api/payments/create-intent` | Create a Stripe Payment Intent | **Player** |
| `POST` | `/api/matches/{id}/result` | Submit a match result | **Player** |
| `GET` | `/api/leaderboard/global` | Retrieve the global leaderboard | No |


### Configuration

| Variable | Description | Default / Example |
|----------|-------------|-------------------|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/tournament_db` |
| `SPRING_DATASOURCE_USERNAME` | Database Username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database Password | - |
| `SPRING_REDIS_HOST` | Redis Server Host | `localhost` |
| `SPRING_REDIS_PORT` | Redis Server Port | `6379` |
| `STRIPE_SECRET_KEY` | Stripe Private API Key | `sk_test_...` |
| `STRIPE_WEBHOOK_SECRET` | Stripe Webhook Signing Secret | `whsec_...` |
| `SPRING_PROFILES_ACTIVE` | Active Spring Profile | `dev` |