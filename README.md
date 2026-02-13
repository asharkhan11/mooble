# 🚀 Mooble Backend (Spring Boot)

Mooble Backend is a **scalable, production-grade Spring Boot REST API** that powers the Mooble platform — a complete **Tuition & Learning Management System** for coaching institutes, tuitions, and education centers.

This backend handles:
- Authentication & Authorization
- Multi-tenant tuition management
- Courses, Subjects, Classes, Sessions
- Students, Teachers, Admins
- Attendance, Assignments, Submissions
- Resource management
- Announcements & communication
- Subscription & plan limits
- High-performance, scalable APIs

---

## 🧱 Tech Stack

- **Language:** Java 17+
- **Framework:** Spring Boot
- **Security:** Spring Security + JWT
- **ORM:** Hibernate / JPA
- **Database:** MySQL / PostgreSQL
- **Caching:** Redis
- **Messaging:** Kafka
- **Async & Concurrency:** Executors, Schedulers, @Async
- **Resilience:** Resilience4j (Circuit Breaker, Retry, Rate Limiter)
- **HTTP Clients:** WebClient, Feign
- **Architecture:** Layered + Domain-driven design
- **Deployment:** Docker / Cloud / VPS
- **Reverse Proxy:** Cloudflare / Nginx

---

## 🏗️ Architecture Overview

Controller (REST APIs)
↓
Service (Business Logic)
↓
Repository (JPA / Hibernate)
↓
Database

yaml
Copy code

With:
- DTO separation
- Validation layer
- Global exception handling
- Interceptors & filters
- Role-based access control

---

## 👥 User Roles

- **ADMIN**
- **TEACHER**
- **STUDENT**

Each role has:
- Strict permission control
- Dedicated APIs
- Secure access via JWT

---

## 📦 Core Modules

- Authentication & Authorization
- Tuition Management
- Course & Subject Management
- Class & Session Scheduling
- Student & Teacher Management
- Attendance System
- Assignment & Submission System
- Resource & Folder Management
- Announcements & Broadcast
- Subscription & Limits Engine
- Audit & Logging

---

## 🔐 Security

- JWT-based authentication
- Role-based access control (RBAC)
- Request filters & interceptors
- Secure password hashing
- API-level authorization
- CORS & rate limiting support

---

## ⚙️ Setup Instructions

### 1️⃣ Prerequisites

- Java 17+
- Maven
- MySQL or PostgreSQL
- Redis (optional but recommended)
- Kafka (optional, for async flows)

---

### 2️⃣ Clone the Repository

git clone https://github.com/asharkhan11/mooble.git
cd mooble-backend

---

### 3️⃣ Configure Database

Update in:

application.yml

or

application.properties

Example:

spring.datasource.url=jdbc:mysql://localhost:3306/mooble
spring.datasource.username=root
spring.datasource.password=your_password


---

### 4️⃣ Run the Application

mvn spring-boot:run
or
java -jar target/mooble.jar

---

## 🧪 Testing

- Unit tests: JUnit
- Integration tests: Spring Boot Test
- API testing: Postman / Swagger

---

## 📊 Performance & Scalability

- Redis caching for hot data
- Kafka for async processing
- Connection pooling
- Optimized Hibernate queries
- Pagination everywhere
- Lazy loading & DTO mapping
- Circuit breakers for fault tolerance

---

## 🌐 Frontend Integration

- Flutter App (Mooble Frontend)
- Token-based authentication
- REST-based communication
- Multi-tenant ready

---

## 🗺️ Roadmap

- 🔔 Push Notifications
- 💬 In-app Chat
- 📊 Advanced Analytics
- 💳 Payment Gateway
- 🎥 Live Class Integration
- 📦 Microservices split (future)

---

## 📜 License

This project is **proprietary software** owned by **Mooble**.  
Unauthorized copying, modification, or distribution is strictly prohibited.

---

## 👨‍💻 Author

**Ashar**  
Java Backend Architect & Full Stack Developer  
Creator of **Mooble Platform**
