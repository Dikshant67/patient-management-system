<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5%20%2F%204.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-17%20%2F%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/gRPC-1.69+-4285F4?style=for-the-badge&logo=google&logoColor=white" />
  <img src="https://img.shields.io/badge/Apache%20Kafka-3.3-231F20?style=for-the-badge&logo=apachekafka&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-Database-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" />
</p>

# 🏥 Patient Management System — Microservices Architecture

A production-style **microservices-based Patient Management System** built with **Spring Boot**, featuring inter-service communication via **gRPC** and **Apache Kafka**, secured with **JWT authentication**, and orchestrated through a **Spring Cloud API Gateway**. Each service is independently deployable with its own **Dockerfile** and follows clean architecture principles.

---

## 📑 Table of Contents

- [Architecture Overview](#-architecture-overview)
- [Microservices](#-microservices)
- [Tech Stack & Versions](#-tech-stack--versions)
- [Inter-Service Communication](#-inter-service-communication)
- [API Endpoints](#-api-endpoints)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Environment Variables](#-environment-variables)
- [API Documentation](#-api-documentation)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🏗 Architecture Overview

```
                              ┌──────────────────┐
                              │     Client /      │
                              │   Postman / UI    │
                              └────────┬─────────┘
                                       │
                                       ▼
                          ┌────────────────────────┐
                          │   API Gateway (:4004)   │
                          │  Spring Cloud Gateway   │
                          │  (JWT Validation Filter)│
                          └────┬──────────┬────────┘
                               │          │
                  ┌────────────┘          └───────────────┐
                  ▼                                       ▼
     ┌────────────────────┐                 ┌────────────────────┐
     │ Auth Service (:4005)│                │Patient Service(:4000│
     │  Spring Security    │                │  Spring Data JPA    │
     │  JWT Generation     │                │  CRUD Operations    │
     │  Token Validation   │                └──┬──────────┬──────┘
     └────────────────────┘                    │          │
                                     gRPC     │          │  Kafka
                                   (sync)     │          │ (async)
                                     ▼        │          ▼
                          ┌──────────────────┐│  ┌──────────────────┐
                          │Billing Svc (:4001)││  │Analytics Svc     │
                          │gRPC Server(:9001) ││  │Kafka Consumer    │
                          │Create Billing Acc ││  │Event Processing  │
                          └──────────────────┘│  └──────────────────┘
                                              │
                                              ▼
                                    ┌──────────────────┐
                                    │   PostgreSQL DB   │
                                    │  (Patient Data)   │
                                    └──────────────────┘
```

The system follows a **microservices architecture** where each service has a single responsibility:

1. **API Gateway** — Single entry point; routes requests and validates JWT tokens
2. **Auth Service** — Handles user authentication and JWT token issuance
3. **Patient Service** — Core business logic for patient CRUD operations
4. **Billing Service** — Creates billing accounts via gRPC when a patient is registered
5. **Analytics Service** — Consumes Kafka events for analytics and audit processing

---

## 🧩 Microservices

### 1. API Gateway (`api-gateway`)

| Property | Value |
|----------|-------|
| **Port** | `4004` |
| **Framework** | Spring Cloud Gateway (WebFlux) |
| **Spring Boot** | `4.1.1` |
| **Spring Cloud** | `2025.1.3` |
| **Responsibility** | Route requests to downstream services, validate JWT via a custom `JwtValidationGatewayFilterFactory` |

**Routes configured:**
| Route | Target | Auth Required |
|-------|--------|---------------|
| `/auth/login`, `/auth/validate` | Auth Service `:4005` | ❌ |
| `/api/patients/**` | Patient Service `:4000` | ✅ (JWT) |
| `/api-docs/patients` | Patient Service OpenAPI | ❌ |
| `/api-docs/auth` | Auth Service OpenAPI | ❌ |

---

### 2. Auth Service (`auth-service`)

| Property | Value |
|----------|-------|
| **Port** | `4005` |
| **Framework** | Spring Boot + Spring Security |
| **Spring Boot** | `4.1.1` |
| **Java** | `17` |
| **Responsibility** | User authentication, JWT generation & validation |

**Key Components:**
- `SecurityConfig` — Spring Security filter chain configuration
- `AuthController` — Login and token validation endpoints
- `JwtUtil` — JWT creation and verification using **JJWT 0.12.6**
- `UserService` / `UserRepository` — User persistence with JPA + PostgreSQL

---

### 3. Patient Service (`patient-service`)

| Property | Value |
|----------|-------|
| **Port** | `4000` |
| **Framework** | Spring Boot + Spring Data JPA |
| **Spring Boot** | `3.5.14` |
| **Java** | `21` |
| **Responsibility** | Patient CRUD, triggers billing via gRPC, publishes Kafka events |

**Key Components:**
- `PatientController` — REST endpoints for patient management
- `PatientService` — Business logic with validation, gRPC calls, and Kafka publishing
- `BillingServiceGrpcClient` — Synchronous gRPC client to billing service
- `KafkaProducer` — Publishes `PATIENT_CREATED` events to the `patient` topic
- `PatientMapper` — DTO ↔ Entity mapping
- `GlobalExceptionHandler` — Centralized error handling

**Patient Entity fields:** `id` (UUID), `name`, `email` (unique), `address`, `dateOfBirth`, `registered_date`

---

### 4. Billing Service (`billing-service`)

| Property | Value |
|----------|-------|
| **HTTP Port** | `4001` |
| **gRPC Port** | `9001` |
| **Framework** | Spring Boot + gRPC Spring Boot Starter |
| **Spring Boot** | `3.5.14` |
| **Java** | `17` |
| **Responsibility** | Creates billing accounts for newly registered patients |

**gRPC Service Definition:**
```protobuf
service BillingService {
  rpc CreateBillingAccount (BillingRequest) returns (BillingResponse);
}
```

---

### 5. Analytics Service (`analytics-service`)

| Property | Value |
|----------|-------|
| **Port** | `4002` |
| **Framework** | Spring Boot + Spring Kafka |
| **Spring Boot** | `3.5.14` |
| **Java** | `17` |
| **Responsibility** | Consumes patient events from Kafka for analytics processing |

**Kafka Consumer:** Listens on topic `patient` with group ID `analytics-group`, deserializes Protobuf `PatientEvent` messages.

---

## ⚙ Tech Stack & Versions

| Technology | Version | Purpose |
|---|---|---|
| **Java** | 17 / 21 | Primary language |
| **Spring Boot** | 3.5.14 / 4.1.1 | Application framework |
| **Spring Cloud** | 2025.1.3 | API Gateway (WebFlux) |
| **Spring Security** | (managed by Spring Boot) | Authentication & Authorization |
| **Spring Data JPA** | (managed by Spring Boot) | ORM / Data access |
| **PostgreSQL** | Latest | Production database |
| **H2 Database** | (embedded) | In-memory DB for local dev/testing |
| **gRPC** | 1.69.0+ | Synchronous inter-service communication |
| **Protobuf** | 4.29.1 | Message serialization (gRPC & Kafka) |
| **Apache Kafka** | 3.3.0 (client) | Asynchronous event streaming |
| **JJWT** | 0.12.6 | JWT token generation & validation |
| **SpringDoc OpenAPI** | 2.7.0 / 3.1.1 | Swagger UI & API documentation |
| **Lombok** | (managed by Spring Boot) | Boilerplate reduction |
| **Maven** | 3.9.9 | Build tool |
| **Docker** | Multi-stage builds | Containerization |
| **Eclipse Temurin** | JDK 21 | Docker base image |

---

## 🔗 Inter-Service Communication

This project demonstrates **two communication patterns** commonly used in microservice architectures:

### Synchronous — gRPC

```
Patient Service ──── gRPC (CreateBillingAccount) ────► Billing Service
```

- When a new patient is created, the Patient Service makes a **synchronous gRPC call** to the Billing Service to create a billing account.
- Uses **Protocol Buffers** for efficient, type-safe serialization.
- The gRPC client in the Patient Service connects to `billing-service:9001`.

### Asynchronous — Apache Kafka

```
Patient Service ──── Kafka Topic: "patient" ────► Analytics Service
```

- After patient creation, a `PATIENT_CREATED` event (serialized as Protobuf) is published to the `patient` Kafka topic.
- The Analytics Service consumes these events asynchronously for processing.
- Uses **byte array serialization** with Protobuf for the message payload.

### Gateway Authentication Flow

```
Client ──► API Gateway ──► Auth Service (/validate) ──► Patient Service
```

- The API Gateway intercepts requests to protected routes (e.g., `/api/patients/**`).
- A custom `JwtValidationGatewayFilterFactory` extracts the `Authorization` header and forwards it to the Auth Service's `/validate` endpoint.
- Only upon successful validation does the gateway forward the request to the downstream service.

---

## 📡 API Endpoints

All requests go through the **API Gateway** at `http://localhost:4004`.

### Authentication

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/auth/login` | Authenticate user & get JWT token | ❌ |
| `GET` | `/auth/validate` | Validate an existing JWT token | 🔒 Bearer Token |

**Login Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

### Patient Management

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/api/patients` | List all patients | 🔒 Bearer Token |
| `POST` | `/api/patients` | Create a new patient | 🔒 Bearer Token |
| `PUT` | `/api/patients/{id}` | Update an existing patient | 🔒 Bearer Token |
| `DELETE` | `/api/patients/{id}` | Delete a patient | 🔒 Bearer Token |

**Create Patient Request Body:**
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "address": "123 Main Street",
  "dateOfBirth": "1995-09-09",
  "registeredDate": "2026-01-15"
}
```

### API Documentation

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api-docs/patients` | Patient Service OpenAPI spec |
| `GET` | `/api-docs/auth` | Auth Service OpenAPI spec |

---

## 📂 Project Structure

```
patient-management/
├── api-gateway/                    # Spring Cloud Gateway (entry point)
│   ├── src/main/java/com/pm/apigateway/
│   │   ├── config/                 # WebClient configuration
│   │   ├── exception/              # JWT validation exceptions
│   │   └── filter/                 # JwtValidationGatewayFilterFactory
│   ├── src/main/resources/
│   │   └── application.yml         # Gateway routes & config
│   └── Dockerfile
│
├── auth-service/                   # Authentication & JWT service
│   ├── src/main/java/com/pm/authservice/
│   │   ├── config/                 # Spring Security configuration
│   │   ├── controller/             # AuthController (login, validate)
│   │   ├── dto/                    # LoginRequestDTO, LoginResponseDTO
│   │   ├── exception/              # InvalidCredentialException
│   │   ├── model/                  # User entity
│   │   ├── repository/             # UserRepository (JPA)
│   │   ├── service/                # AuthService, UserService
│   │   └── util/                   # JwtUtil
│   └── Dockerfile
│
├── patient-service/                # Core patient management service
│   ├── src/main/java/org/pm/patientservice/
│   │   ├── controller/             # PatientController (CRUD REST)
│   │   ├── dto/                    # Request/Response DTOs + validators
│   │   ├── exception/              # Custom exceptions & global handler
│   │   ├── grpc/                   # BillingServiceGrpcClient
│   │   ├── kafka/                  # KafkaProducer
│   │   ├── mapper/                 # PatientMapper
│   │   ├── model/                  # Patient entity (JPA)
│   │   ├── repository/             # PatientRepository (JPA)
│   │   └── service/                # PatientService (business logic)
│   ├── src/main/proto/
│   │   ├── billing_service.proto   # gRPC contract for billing
│   │   └── patient-event.proto     # Protobuf for Kafka events
│   └── Dockerfile
│
├── billing-service/                # Billing account management (gRPC)
│   ├── src/main/java/org/pm/billingservice/
│   │   └── grpc/                   # BillingGrpcService
│   ├── src/main/proto/
│   │   └── billing_service.proto   # gRPC service definition
│   └── Dockerfile
│
├── analytics-service/              # Event-driven analytics consumer
│   ├── src/main/java/com/pm/analyticsservice/
│   │   └── kafka/                  # KafkaConsumer
│   ├── src/main/proto/
│   │   └── patinent.event.proto    # Protobuf event schema
│   └── Dockerfile
│
├── api-requests/                   # HTTP request collections (IntelliJ)
│   └── patient-service/            # .http files for testing
│
├── grpc-requests/                  # gRPC request collections
│   └── billing-service/            # .http files for gRPC testing
│
└── docs/                           # Architecture diagrams (Excalidraw)
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 21** (or 17 for some services)
- **Maven 3.9+**
- **Docker & Docker Compose**
- **PostgreSQL** (or use embedded H2 for local development)
- **Apache Kafka** (with Zookeeper or KRaft)

### 1. Clone the Repository

```bash
git clone https://github.com/Dikshant67/patient-management-system.git
cd patient-management-system
```

### 2. Run with Docker (Recommended)

Each service has a multi-stage `Dockerfile`. You can build and run them individually:

```bash
# Build a service
cd patient-service
docker build -t patient-service .

# Run the container
docker run -p 4000:4000 patient-service
```

> **Tip:** Create a `docker-compose.yml` to orchestrate all services together with PostgreSQL, Kafka, and Zookeeper.

### 3. Run Locally (Without Docker)

```bash
# Navigate to a service directory
cd patient-service

# Build the service
./mvnw clean package

# Run the service
./mvnw spring-boot:run
```

> **Note:** For local development without Docker, uncomment the H2 database configuration in `patient-service/src/main/resources/application.properties` to use an in-memory database.

### 4. Service Startup Order

For a full system run, start services in this order:

1. **PostgreSQL** & **Kafka** (infrastructure)
2. **Auth Service** (`:4005`)
3. **Billing Service** (`:4001`, gRPC `:9001`)
4. **Analytics Service** (`:4002`)
5. **Patient Service** (`:4000`)
6. **API Gateway** (`:4004`)

---

## 🔐 Environment Variables

| Variable | Service | Default | Description |
|----------|---------|---------|-------------|
| `DB_URL` | Patient Service | `jdbc:h2:mem:testdb` | Database JDBC URL |
| `DB_CLASS` | Patient Service | `org.h2.Driver` | JDBC driver class |
| `DB_USERNAME` | Patient Service | `admin_viewer` | Database username |
| `DB_PASSWORD` | Patient Service | `password` | Database password |
| `DB_PLATFORM` | Patient Service | `org.hibernate.dialect.H2Dialect` | Hibernate dialect |
| `billing.service.address` | Patient Service | `localhost` | Billing service hostname |
| `billing.service.grpc.port` | Patient Service | `9001` | Billing gRPC port |
| `auth.service.url` | API Gateway | — | Auth service base URL |

---

## 📖 API Documentation

Each service exposes **Swagger UI** powered by SpringDoc OpenAPI:

| Service | Direct URL | Via Gateway |
|---------|-----------|-------------|
| Patient Service | `http://localhost:4000/swagger-ui.html` | `http://localhost:4004/api-docs/patients` |
| Auth Service | `http://localhost:4005/swagger-ui.html` | `http://localhost:4004/api-docs/auth` |

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'feat: add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Branch Naming Convention

| Prefix | Purpose |
|--------|---------|
| `feature/` | New features |
| `fix/` | Bug fixes |
| `docs/` | Documentation updates |
| `refactor/` | Code refactoring |

---

## 📄 License

This project is open-source and available for learning and educational purposes.

---

<p align="center">
  <strong>Built with ❤️ using Spring Boot Microservices</strong>
</p>
