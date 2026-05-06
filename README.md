# 🏥 MediFlow AI: Intelligent Patient Queue Management System

MediFlow AI is a professional-grade healthcare solution designed to optimize patient flow in clinical environments. Unlike traditional "First-In-First-Out" systems, MediFlow AI leverages a dynamic priority scoring algorithm to ensure that critical cases receive immediate attention while maintaining a transparent and communicative experience for all patients.

---

## 🌟 Key Features (Receptionist Module)

### 1. AI-Driven Priority Scoring (BF05)

The core of the system is an intelligent scoring engine that calculates a patient's priority in real-time. The system uses the following formula to rank patients in the queue:

$$PriorityScore = (\text{UrgencyLevel} \times 10) + \text{AgeFactor} + \text{AppointmentBonus}$$

- **Urgency Level:** Categorized into `LOW`, `MEDIUM`, and `HIGH` based on initial nursing triage.
- **Age Factor:** Automatic priority bonuses for vulnerable populations (infants and seniors).
- **Appointment Bonus:** A +15 point incentive for patients who scheduled their visit in advance, balancing emergency needs with scheduled care.

### 2. Automated Smart Notifications (BF07 & BF08)

- **Instant Digital Ticket:** Upon registration, patients receive a professional email confirmation via SMTP. This includes their unique ticket number, current position in line, and a dynamic estimated waiting time.
- **Proximity Alert (The Scheduler):** A background "Robot" (Spring Scheduler) monitors the queue every 30 seconds. When a patient is within 5 minutes of their turn, the system sends an automated "Almost Ready" reminder.

### 3. Queue Management & Data Integrity

- **Absence Handling:** Receptionists can mark patients as "Absent". The system instantly archives the ticket and shifts the entire queue forward, recalculating wait times for all remaining patients.
- **Historical Traceability:** A dedicated History View tracks all `Completed` and `Absent` patients, recording precise arrival and departure times for clinic performance analytics.

### 4. Doctor Management (Doctor's Module)

- **Doctor Registration & Assignment:** Administrators can register new doctors and assign them to specific medical services (e.g., Cardiology, General Medicine).
- **Consultation Lifecycle:** Receptionists can dynamically assign waiting tickets to specific doctors, ensuring a smooth transition from the waiting room to the consultation phase.

---

## 🛠️ Technical Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 17, Spring Boot 3.x, Spring Data JPA |
| **Frontend** | JavaFX 17 (MVC Architecture with FXML) |
| **Database** | MySQL (Relational Schema for Patients, Services, and Tickets) |
| **REST API** | Asynchronous communication via Java `HttpClient` |
| **Email Engine** | Java Mail Sender with `MimeMessageHelper` for professional branding |
| **Task Automation** | Spring `@Scheduled` tasks for real-time queue monitoring |

---

## 📂 System Architecture

### 🔹 Backend Logic

| Component | Responsibility |
|---|---|
| `TicketService` | Orchestrates the lifecycle of a ticket, ensuring data is calculated before notification dispatch |
| `PriorityService` | The mathematical engine responsible for scoring and wait-time estimations |
| `EmailService` | A dedicated service for generating branded HTML/Plain-text notifications |

### 🔹 Frontend Logic

| Component | Responsibility |
|---|---|
| `DashboardController` | Manages the live queue display, search functionality, and patient registration |
| `HistoryView` | A modal window providing administrative insight into past consultations |

---

## 🚀 Getting Started

### 1. Database Setup

- Create a MySQL database named `mediflow_db`.
- Configure `src/main/resources/application.properties` with your MySQL credentials and SMTP Gmail settings:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mediflow_db
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_GMAIL_ADDRESS
spring.mail.password=YOUR_APP_PASSWORD
```

### 2. Run the Backend

Execute `BackendApplication.java`. Ensure `@EnableScheduling` is active on the main class:

```java
@SpringBootApplication
@EnableScheduling
public class BackendApplication { ... }
```

### 3. Run the Frontend

Launch the JavaFX application. The dashboard will automatically sync with the backend at `http://localhost:8080`.

---

## 🚀 Project Status

| Module | Status |
|---|---|
| Receptionist Module | ✅ 100% Complete |
| Doctor's Module | ✅ 100% Complete |

**Next Milestone:** Administrator Module & Enhanced Analytics Dashboard.

---

> *MediFlow AI – Engineering smarter care, one queue at a time.*