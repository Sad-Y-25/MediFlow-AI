<div align="center">

# 🏥 MediFlow AI
### Intelligent Patient Queue Management System

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.6-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-17-2196F3?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)

*An enterprise-grade, decoupled client-server healthcare solution engineered to optimize patient flow in clinical environments.*

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Priority Scoring Algorithm](#-priority-scoring-algorithm)
- [System Architecture](#-system-architecture)
- [Technology Stack](#-technology-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Deployment Profiles](#-deployment-profiles)
- [Project Status](#-project-status)

---

## 🔍 Overview

MediFlow AI replaces obsolete First-In-First-Out (FIFO) queue mechanics with a **dynamic, multi-factor priority scoring algorithm**, ensuring that high-acuity medical cases receive immediate attention while systematically preventing lower-risk patient starvation. The system delivers a transparent, secure, and communicative experience for staff and patients alike.

The platform is built around three fully operational roles and modules:

| Role / Module | Dashboard Controller | Responsibilities |
|---------------|---------------------|-----------------|
| 👩‍💼 **Receptionist** | `DashboardController` | Patient check-in, NLP triage, dropdown doctor assignment, session logout, queue & absence management |
| 👨‍⚕️ **Doctor** | `DoctorDashboardController` | Private queue consultation, background polling, patient acceptance, consultation lifecycle |
| 📺 **Public Monitor** | `WaitingRoomController` | Read-only lounge TV display, 10s auto-refresh polling, partial data anonymization |

---

## ✨ Key Features

### 🧠 1. Real-Time Edge NLP Triage
A text property listener parses patient complaint tokens on the fly inside the receptionist module. Detecting high-acuity medical keywords (e.g., *chest pain*, *breathing issues*, *blood*) triggers a localized override — automatically selecting **HIGH urgency** and wrapping the input component in high-contrast warning alerts before form submission.

### 🧮 2. Doctor-Specific Isolated Queues & Wait Times
Unlike a general single-file line, the calculation engine dynamically **cloisters wait-time metrics per doctor and medical service**. When multiple practitioners are available across separate offices, they process consultations simultaneously. The system computes separate position counts and wait-time increments for each distinct sub-queue.

### 👤 3. Privacy-Compliant Patient Anonymization (GDPR)
The Public Lounge TV interface features strict **data protection formatting rules**. Before rendering names onto the public board, full strings are processed on the fly — for example, `"Jean Dupont"` is partially masked to its clinical identifier layout: **`J. DUPONT`** — ensuring patient confidentiality at all times.

### 🎫 4. Sequential Human-Readable Ticket Shortcodes
Raw millisecond timestamp identifiers are replaced with clean, professional sequential shortcodes. By forcing an initial database save, the transaction extracts the relational auto-increment primary key to generate tokens such as **`TCK-001`**, **`TCK-002`**, and **`TCK-005`**.

### 📧 5. Automated Smart Notifications
- **Instant Digital Ticket** — Upon registration, patients receive a structured HTML/plain-text email via SMTP containing their sequential ticket number, current position in line, and a dynamic estimated waiting time.
- **Proximity Cron Alert** — A background Spring Scheduler thread monitors the database every **30 seconds**. When a patient's estimated wait narrows to **≤ 5 minutes**, an automated *"Almost Ready"* reminder is fired to bring them closer to the consultation area.

### 💼 6. Secure Dropdown Doctor Assignment
The interface discards error-prone manual numeric ID text fields. The assignment system invokes an asynchronous network fetch to populate a stylized `ComboBox` displaying only active, registered practitioners alongside their service labels (e.g., `Dr. Mahdi [Cardiologie]`), eliminating typing errors and invalid assignments.

### ⚡ 7. Anti-Starvation Queue Engine
Continuous wait-time weighting (+1 point per 5 minutes) prevents non-urgent patients from stalling indefinitely, guaranteeing every patient eventually reaches the front regardless of their urgency level.

### 🗂️ 8. Queue Management & Historical Traceability
- **Absence Handling** — Receptionists can mark patients as *"Absent"*. The system instantly archives the ticket and shifts the entire queue forward, recalculating wait times for all remaining patients.
- **History View** — A dedicated modal window tracks all `Completed` and `Absent` patients, recording precise arrival and departure timestamps for clinic performance analytics.

---

## 📐 Priority Scoring Algorithm

Every incoming patient is assigned a dynamic score calculated in real-time by `PriorityService` using the following composite model:

$$\text{PriorityScore} = \text{UrgencyPoints} + \text{AppointmentBonus} + \text{VulnerabilityFactor} + \text{WaitTimeWeight}$$

| Component | Description | Value |
|-----------|-------------|-------|
| 🚨 **Urgency Points** | Mapped from clinical triage evaluation tokens | HIGH = +60 · MEDIUM = +30 · LOW = +10 |
| 📅 **Appointment Bonus** | Awarded to pre-scheduled visits, balancing emergency walk-ins with scheduled care | +15 pts |
| 🧓 **Vulnerability Factor** | Demographic protection weight for seniors (age > 60) and infants | +10 pts |
| ⏱️ **Wait-Time Weight** | Anti-starvation increment, applied continuously in the background | +1 pt per 5 min waited |

---

## 🏛️ System Architecture

MediFlow AI follows a **decoupled, layered architecture** with strict separation of concerns across six layers:

```
┌─────────────────────────────────────────────────────────────┐
│             FXML / CSS  —  Presentation Layer               │
│      Renders layout states · Layout structures & Styles     │
├─────────────────────────────────────────────────────────────┤
│           JavaFX Controllers  —  View Action Layer          │
│   Captures events · Background polling · Platform.runLater  │
├─────────────────────────────────────────────────────────────┤
│         API Services  —  Client Remote Consumer Layer       │
│      Gson serialization · java.net.http.HttpClient          │
├─────────────────────────────────────────────────────────────┤
│         REST Controllers  —  Network Boundary Layer         │
│     Spring endpoints · @CrossOrigin · Session mappings      │
├─────────────────────────────────────────────────────────────┤
│          Spring Services  —  Business Engine Layer          │
│    Priority scoring · @Transactional · SMTP scheduling      │
├─────────────────────────────────────────────────────────────┤
│        JPA Repositories / MySQL  —  Persistence Layer       │
│           ACID integrity · Relational query management      │
└─────────────────────────────────────────────────────────────┘
```

### 🔹 Backend Service Responsibilities

| Component | Responsibility |
|-----------|---------------|
| `TicketService` | Orchestrates the full lifecycle of a ticket — structural parsing, doctor-specific queue partitioning, and cron notification dispatch |
| `PriorityService` | The mathematical engine responsible for computing live priority scores and per-doctor anti-starvation weights |
| `EmailService` | Dedicated service for generating formatted HTML/plain-text SMTP notification dispatches |
| `AuthService` | Handles user authentication and session management via Spring Security |
| `DoctorService` | Manages doctor registration workflows across safe database isolation blocks (`@Transactional`) |

### 🔹 Frontend Controller Responsibilities

| Component | Responsibility |
|-----------|---------------|
| `DashboardController` | Manages the live receptionist view, real-time NLP text properties, and secure ComboBox doctor assignments |
| `DoctorDashboardController` | Handles doctor-side multi-threaded queue polling, patient acceptance, and consultation logs |
| `WaitingRoomController` | Controls the read-only patient monitor board — auto-refresh timer, GDPR string masking, and live position display |
| `DoctorsController` | Administrative panel for setting up doctor records linked to clinical divisions |
| `LoginController` | Authenticates user credentials and populates the thread-safe `SessionContext` Singleton |

---

## 🛠️ Technology Stack

| Layer | Component | Frameworks / Libraries |
|-------|-----------|----------------------|
| **Backend API Core** | REST Server | Java 17, Spring Boot 4.0.6, Spring Security |
| **Data Access / ORM** | Database Layer | Spring Data JPA, Hibernate ORM Core 7.2.12.Final |
| **Database Engine** | Storage | MySQL 8.0, Adminer Graphical DB Interface |
| **Desktop UI** | Rich Client | OpenJFX (JavaFX 17), FXML Layouts, Native CSS |
| **Data Serialization** | JSON Mapping | Google Gson 2.10.1 (with custom `LocalDateTime` adapters) |
| **Network Consumer** | HTTP Client | Java Native Async `java.net.http.HttpClient` |
| **Notification Relay** | Email Engine | Java Mail Sender, `MimeMessageHelper`, Jakarta Mail via Secure SMTP |
| **Task Automation** | Scheduler | Spring `@Scheduled` tasks for real-time queue monitoring |
| **DevOps Pipeline** | Containerization | Docker Desktop, Multi-Stage Isolated Production Builds |

---

## 📂 Project Structure

```
mediflow-workspace/
│
├── backend/                               # SPRING BOOT API SERVER
│   ├── src/main/java/com/mediflow/
│   │   ├── config/
│   │   │   └── SecurityConfig.java        # Infrastructure & Security Context
│   │   ├── controller/                    # REST Endpoints (HTTP mapping)
│   │   │   ├── AuthController.java
│   │   │   ├── DoctorController.java
│   │   │   └── TicketController.java
│   │   ├── entity/                        # JPA Database Mapping Models
│   │   │   ├── User.java
│   │   │   ├── Doctor.java
│   │   │   ├── MedicalService.java
│   │   │   ├── Patient.java
│   │   │   └── Ticket.java
│   │   ├── repository/                    # Database Abstraction (Spring Data JPA)
│   │   │   ├── UserRepository.java
│   │   │   ├── DoctorRepository.java
│   │   │   ├── MedicalServiceRepository.java
│   │   │   └── TicketRepository.java
│   │   ├── service/                       # Business Logic & Schedulers
│   │   │   ├── AuthService.java
│   │   │   ├── DoctorService.java
│   │   │   ├── TicketService.java
│   │   │   ├── PriorityService.java       # ⭐ Priority Engine
│   │   │   └── EmailService.java          # ⭐ SMTP Email Service
│   │   └── BackendApplication.java        # Spring Main Entry Point
│   │
│   ├── src/main/resources/
│   │   ├── application.properties         # Root Settings
│   │   ├── application-local.properties   # IntelliJ Local Profile
│   │   └── application-docker.properties  # Production Container Profile
│   │
│   ├── Dockerfile                         # Multi-Stage Build Configuration
│   ├── docker-compose.yml                 # Multi-Container Stack (App, DB, Adminer)
│   └── pom.xml                            # Backend Dependency Manager
│
├── frontend/                              # RICH CLIENT DESKTOP UI (JAVAFX)
│   ├── src/main/java/com/mediflow/ui/
│   │   ├── api/                           # HTTP API Client Consumers
│   │   │   ├── AuthApiService.java
│   │   │   └── DoctorApiService.java
│   │   ├── controller/                    # JavaFX View Controllers (MVC)
│   │   │   ├── LoginController.java
│   │   │   ├── DashboardController.java        # Receptionist Operations
│   │   │   ├── DoctorDashboardController.java  # Doctor Actions & Polling
│   │   │   ├── WaitingRoomController.java      # ⭐ Patient Lounge Monitor
│   │   │   └── DoctorsController.java          # Admin Panel
│   │   ├── entity/                        # Client-Side POJO Data Targets
│   │   │   ├── User.java
│   │   │   ├── Doctor.java
│   │   │   ├── MedicalService.java
│   │   │   ├── Patient.java
│   │   │   └── Ticket.java
│   │   ├── util/
│   │   │   └── SessionContext.java        # Thread-Safe Session Singleton
│   │   └── App.java                       # JavaFX Window Launcher
│   │
│   ├── src/main/resources/com/mediflow/ui/
│   │   ├── LoginView.fxml
│   │   ├── Dashboard.fxml
│   │   ├── DoctorDashboard.fxml
│   │   ├── WaitingRoom.fxml               # ⭐ Monitor View
│   │   ├── DoctorsView.fxml
│   │   └── style.css                      # Application Stylesheet
│   │
│   ├── module-info.java                   # JPMS Strict Encapsulation Module
│   └── pom.xml                            # Frontend Dependency Manager
│
└── DB.sql                                 # Relational Database Initialization Schema
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven
- MySQL 8.0 (or Docker Desktop for the containerized setup)
- IntelliJ IDEA (recommended for hybrid dev setup)

### Step 1 — Database Setup

Create a MySQL database named `mediflow_db`, then configure `backend/src/main/resources/application.properties` with your credentials:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/mediflow_db
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD

# SMTP Email (Gmail)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_GMAIL_ADDRESS
spring.mail.password=YOUR_APP_PASSWORD
```

> ⚠️ For Gmail, use an **App Password** — not your regular account password. Enable 2FA on your Google account and generate one at *Google Account → Security → App Passwords*.

### Step 2 — Run the Backend

Execute `BackendApplication.java`. Ensure `@EnableScheduling` is active on the main class to power the proximity alert cron job:

```java
@SpringBootApplication
@EnableScheduling
public class BackendApplication { ... }
```

The backend will start at `http://localhost:8080`.

### Step 3 — Run the Frontend

Launch the JavaFX application via `App.java`. The dashboard will automatically connect to the backend and begin syncing the live queue. To open the **Patient Lounge Monitor**, launch the `WaitingRoom.fxml` view — it self-refreshes every 10 seconds with no staff interaction required.

---

## 🐳 Deployment Profiles

The codebase supports two distinct setup environments:

### Option A — Hybrid Developer Setup

Runs MySQL and Adminer inside isolated Docker containers while keeping the Spring Boot backend and JavaFX views locally in IntelliJ for fast hot-reload and debugging.

```bash
# Start the database and management console containers
docker-compose up db adminer
```

Then run `BackendApplication.java` from IntelliJ using the `local` profile (`application-local.properties`).

### Option B — Full Production Setup

Compiles and hosts the entire multi-container stack automatically with a single command:

```bash
docker-compose up --build
```

This orchestrates three services:

| Service | Description |
|---------|-------------|
| 🟢 **App** | Spring Boot API server (compiled via multi-stage Docker build) |
| 🗄️ **DB** | MySQL 8.0 database engine |
| 🖥️ **Adminer** | Graphical DB management interface at `http://localhost:8081` |

---

## 📊 Project Status

| Module | Status |
|--------|--------|
| 👩‍💼 Receptionist Module | ✅ 100% Complete |
| 👨‍⚕️ Doctor's Module | ✅ 100% Complete |
| 📺 Patient Lounge Monitor Module | ✅ 100% Complete |
| 🔧 Administrator Module | 🔄 In Progress |
| 📈 Enhanced Analytics Dashboard | 🔄 Planned |

---

<div align="center">

*MediFlow AI — Engineering smarter care, one queue at a time.*

</div>