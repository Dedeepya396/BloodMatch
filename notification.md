# BloodMatch Notification Subsystem Documentation

## Overview
The BloodMatch Notification Subsystem is a critical component designed to drive donor engagement and provide rapid alerts during medical emergencies. It ensures that the right donors are reached at the right time, minimizing blood shortages and maximizing life-saving opportunities.

**Sender Information:**
- **Email Account:** Jagadeesh (`jagadeeshamudala.111@gmail.com`)
- **Service Provider:** SendGrid
    - SendGrid is used for its ease of integration and reliable delivery.
    - It allows for **300 free emails per day**, which easily accommodates our system's notification requirements (e.g., weekly reminders and emergency alerts).

---

## Design Patterns & Architectural Tactics

### 1. Observer Design Pattern
The core of the notification system is built on the **Observer Pattern**. This behavioral design pattern is used to define a one-to-many dependency between objects so that when one object (the Subject) changes state, all its dependents (Observers) are notified and updated automatically.

In BloodMatch, this ensures that the core business logic (like emergency alerts or weekly schedules) is decoupled from the specific delivery mechanisms (like Email or future SMS/Push).

#### Why we used the Observer Pattern
- **Loose Coupling:** The `NotificationManager` (Subject) does not need to know the implementation details of the observers (like SendGrid API keys or SMTP configuration). It only interacts with the `NotificationObserver` interface.
- **Single Responsibility:** The classes that trigger notifications (e.g., `EmergencyDonorAlertService`) only care about *who* to notify and *what* to say, not *how* it's delivered.
- **Extensibility (Open/Closed Principle):** We can easily add new notification channels (e.g., `SMSNotificationObserver`, `PushNotificationObserver`) simply by creating a new class that implements the interface. We don't need to touch the existing code.
- **Scalability:** Multiple observers can listen to the same event. For example, a single alert could trigger both an email and a log entry simultaneously.

#### Without the Observer Pattern
Without this pattern, the system would suffer from:
- **Tight Coupling:** The logic for sending emails would be hardcoded inside services. If we switched from SendGrid to another provider, we'd have to change code in multiple files.
- **Reduced Flexibility:** Adding a new notification type (like SMS) would require manual code changes in every service that sends alerts.
- **Testing Difficulties:** It would be harder to unit test services because they would be directly dependent on external email APIs.
- **Code Duplication:** Shared delivery logic would likely be duplicated across different parts of the application.

#### Core Components:
- **Subject Interface:** `NotificationSubject` (Defines methods to attach, detach, and notify observers).
- **Concrete Subject:** `NotificationManager` (Manages the list of observers and broadcasts updates. In our Spring implementation, it automatically discovers all beans implementing `NotificationObserver`).
- **Observer Interface:** `NotificationObserver` (Defines the `update()` method that all concrete observers must implement).
- **Concrete Observer:** `EmailNotificationObserver` (Implements the actual delivery logic using the SendGrid API).




### 2. Architectural Tactics & Quality Attributes
To ensure the system is robust, performant, and maintainable, we have implemented several architectural tactics categorized by quality attributes:
### A. Availability (Fault Detection & Recovery)

- The architectural tactic used is **Exception Handling for Fault Isolation and Graceful Degradation**.
- It belongs to the **Availability Tactics** category, specifically under **Fault Detection and Recovery**.
- External API calls are wrapped in **try-catch blocks** to detect failures early and contain them within the notification module, preventing system-wide impact.

### Issue Addressed by Introducing the Tactic:

The notification system relies on the external email service SendGrid. In emergency scenarios, failures such as Network timeouts,Service unavailability & Server errors (e.g., HTTP 500 may occur.

In a naive implementation, such failures would throw exceptions that propagate up the call stack, potentially crashing components like `MatchingService` or `EmergencyDonorAlertService`. As a result, a failure in sending an email could prevent critical operations such as saving blood requests or notifying hospitals, leading to partial or complete system failure for that request.

---

### Allocation Process (Implementation Logic):

In `EmailNotificationObserver.java`, the SendGrid API call (`sg.api(request)`) is enclosed within a `try-catch` block.

- If an error occurs (e.g., `IOException`):
    - The exception is caught immediately
    - Error details (status code and response body) are logged using SLF4J
    - The exception is not re-thrown

This ensures that the failure is isolated within the notification layer and does not interrupt the execution of the remaining business logic.

---

### NFRs Addressed:

### NFR:Availability - Fault Tolerance

**Definition:**

Fault tolerance is the ability of a system to continue operating correctly even when some components fail.

---

**How Exception Handling Addresses It:**

This tactic prevents cascading failures by isolating faults at their source. The system prioritizes critical operations (such as managing blood requests) over non-critical ones (such as sending notifications). As a result, the system continues functioning even if external dependencies fail.

---

**Concrete Example from BloodMatch:**

Suppose a hospital creates an emergency request for 5 units of O− blood.

- **Without Exception Handling:** The email service failure causes an exception, the request is not saved, and the system crashes for the user.
- **With Exception Handling:**The email fails and is logged, but the system successfully saves the request and displays results to the hospital.

The system remains available for its critical functionality

### B. Performance (Resource Management)

- The architectural tactic used is **Scheduling Policy (Periodic Batch Processing)**.
- It belongs to the **Performance Tactics** category, specifically under **Resource Management → Scheduling Policy**.
- The system schedules heavy background tasks at fixed time intervals using cron-based triggers to avoid interference with real-time operations.

---

### Issue Addressed by Introducing the Tactic:

Identifying eligible donors (those who have not donated in the last 90 days) requires scanning the entire donor database, which is a **computationally expensive operation**.

If this task is executed dynamically it introduces **high response latency,**May cause **database contention or locking &** Can lead to **CPU and memory spikes**

Additionally, without controlled scheduling, multiple instances of this operation could run simultaneously, further degrading system performance. This negatively impacts critical real-time functionalities such as emergency blood matching and hospital queries.

---

### Implementation Process (Weekly Eligibility):

In `WeeklyNotificationScheduler.java`, the system uses Spring’s `@Scheduled` annotation with the following cron expression: @Scheduled(cron = "0 0 10 * * MON")

- The task runs **once per week at a fixed time (Monday 10:00 AM),**execution is **controlled and predictable &**heavy computation is shifted to a **background process**

The system uses **time-based scheduling (time-shifting)** to ensure that resource-intensive tasks do not compete with real-time user requests.

---

### NFRs Addressed:

### NFR:Performance – Throughput

**Definition:**

Throughput is the number of requests a system can process within a given time period.

---

**How Scheduling Policy Addresses It:**

By moving heavy computations to scheduled background jobs, the system frees up CPU and database resources for real-time operations. This prevents resource contention between batch processes and user-facing tasks.

As a result the system can handle **more concurrent requests,r**esponse time for critical operations remains low & overall system efficiency improves

---

**Concrete Example from BloodMatch:**

During peak hours, multiple hospitals may request blood simultaneously.

- **Without Scheduling:**The system repeatedly scans the database during requests, causing slow responses and reduced performance.
- **With Scheduling:**The eligibility scan is already completed at 10 AM, and system resources are fully available for handling hospital requests.

The system maintains high throughput for critical operations

### C. Performance (Resource Management)

- The architectural tactic used is **Bounded Processing (Geographic Filtering)**.
- It belongs to the **Performance Tactics** category, specifically under **Resource Management → Bound Processing**.
- The system applies **geographic filtering** by restricting search results to a 20 km radius, thereby limiting the amount of data processed.

---

### Issue Addressed by Introducing the Tactic:

In emergency donor search scenarios, scanning the entire donor database is both inefficient and unnecessary.

- It introduces **high computational cost**
- Produces **irrelevant results** (e.g., donors located far away)
- Increases **response time (latency)**

For high-urgency cases, donors located far from the hospital cannot respond in time. Without limiting the processing scope, the system wastes resources evaluating thousands of records that are not practically useful.

---

### Implementation Process (Emergency Filtering):

In `MatchingService.java` and `EmergencyDonorAlertService.java`, the system applies a geographic constraint before performing detailed matching:

1. The distance between the hospital and each candidate (donor or blood bank) is calculated using `DistanceUtil` (Haversine formula)
2. If the distance exceeds **20 km** (for HIGH urgency), the record is immediately discarded
3. Only nearby candidates are passed to the next stage (blood compatibility and notification logic)

This ensures that the system performs complex computations only on a **small, relevant subset of data**.

---

### NFRs Addressed:

### NFR:Performance – Latency

**Definition:**

Latency is the time taken by the system to respond to a user request.

---

**How Bounded Processing Addresses It:**

By reducing the number of records to be processed, the system minimizes computation time. The working dataset is reduced from the entire database to only geographically relevant entries.

As a result:

- Response time becomes **faster and more consistent**
- System performance remains stable even as the database grows
- Processing overhead is significantly reduced

---

**Concrete Example from BloodMatch:**

In a system with 50,000 donors:

- Only ~30 donors may fall within a 20 km radius
- **Without bounding:**The system processes all 50,000 records, leading to high latency
- **With bounding:**The system processes only 30 records, reducing response time from seconds to milliseconds

The hospital receives results almost instantly during emergencies

### D. Security (Resisting Attacks)

- The architectural tactic used is **Limit Exposure (Secret Externalization)**.
- It belongs to the **Security Tactics** category, specifically under **Resisting Attacks → Limit Exposure**.
- The system uses **configuration externalization** to ensure that sensitive credentials (API keys) are not stored in the source code but are managed securely using environment variables and protected configuration files.

---

### Issue Addressed by Introducing the Tactic:

Hardcoding sensitive credentials, such as the `sendgrid.api-key`, directly in the source code creates a **critical security vulnerability**.

If the repository is accidentally exposed (e.g., pushed to a public GitHub repository):

- Attackers can extract API keys, can misuse services for spam, phishing, or malicious activities & cause financial and reputational damage
- Managing different environments (development, staging, production) becomes difficult

This increases the system’s **attack surface** and risk of compromise.

---

### Implementation Process:

In the BloodMatch system, all sensitive information is **externalized from the codebase**:

- Spring’s `@Value` annotation is used to inject configuration values at runtime
- The `application.properties` file contains placeholders (e.g., `${SENDGRID_API_KEY}`)
- A local `application-secret.properties` file is used during development
- This file is added to `.gitignore` to prevent it from being committed to version control
- In production, secrets are provided through **secure environment variables**

This ensures that:

- No sensitive data is stored in the repository
- Configuration remains flexible across environments
- Secrets can be rotated without modifying the code

---

### NFRs Addressed:

### NFR:Security -Confidentiality

**Definition:**

Confidentiality ensures that sensitive information is accessible only to authorized entities.

**How Limit Exposure Addresses It:**

By separating secrets from the source code, the system ensures that only authorized users with access to the runtime environment can retrieve sensitive credentials.

This approach:

- Prevents exposure of secrets in public repositories
- Reduces the attack surface
- Protects external services from misuse

---

**Concrete Example from BloodMatch:**

If the BloodMatch GitHub repository is accidentally made public:

- **Without limit exposure:**API keys are visible in the code and can be immediately exploited
- **With limit exposure:**Only placeholders like `${SENDGRID_API_KEY}` are visible
Actual keys remain securely stored in environment variables

The system maintains confidentiality and prevents unauthorized access

### E. Modifiability (Localize Changes)

- The architectural tactic used is **Use an Intermediary (Observer Pattern)**.
- It belongs to the **Modifiability Tactics** category, specifically under **Localize Changes → Use an Intermediary**.
- The system uses a central intermediary (`NotificationManager`) to decouple business logic from specific notification delivery mechanisms.

---

### Issue Addressed by Introducing the Tactic:

In the BloodMatch system, multiple events trigger notifications, such as:

- Emergency blood requests
- Weekly eligibility reminders
- User registration confirmations

If notification logic (e.g., email sending via SendGrid) is directly embedded inside each business service, it leads to **tight coupling**.

This creates several problems:

- Any change (e.g., switching email provider) requires modifying multiple files
- Adding new channels (SMS, push notifications) becomes difficult
- Leads to code duplication
- High risk of **ripple effects**, where one change breaks multiple parts of the system

---

### Implementation Process:

The system implements the **Observer pattern** using a central `NotificationManager` (Subject):

- `NotificationManager` maintains a list of `NotificationObserver` instances
- Each observer represents a notification channel (e.g., `EmailNotificationObserver`)

Workflow:

1. A business service (e.g., `MatchingService`) triggers a notification
2. It calls `notify()` on `NotificationManager`
3. The manager iterates through all registered observers
4. Each observer executes its own `update()` method

Using Spring:

- Observers are automatically registered via dependency injection
- Business services remain unaware of specific notification implementations

---

### NFRs Addressed:

### NFR : Modifiability – Extensibility

**Definition:**

Extensibility is the ability of a system to add new features with minimal changes to existing code.

---

**How the Intermediary (Observer Pattern) Addresses It:**

- Localizes all notification logic in observer classes
- Core business logic does not depend on specific implementations
- New functionality can be added without modifying existing services

This follows the **Open/Closed Principle**:

- Open for extension
- Closed for modification

---

**Concrete Example from BloodMatch:**

If the system needs to support WhatsApp notifications:

- A new class `WhatsAppNotificationObserver` is created
- It implements the `NotificationObserver` interface
- No changes are required in:
    - `MatchingService.java`
    - `WeeklyNotificationScheduler.java`
    - Any core business logic

The `NotificationManager` automatically includes the new observer.

New features are added easily without breaking existing code
High extensibility is achieved
---

## Notification Cases

### Case 1: Weekly Eligibility Notifications
**Goal:** Encourage recently eligible donors to return for their next donation.
- **Schedule:** Every **Monday at 10:00 AM**.
- **Trigger:** Handled by `WeeklyNotificationScheduler` using Spring's `@Scheduled` annotation.
- **Why this notification is needed:**
    - **Donor Health & Safety:** Reminds donors of the mandatory 90-day cooldown period between donations to ensure their own recovery.
    - **Proactive Stock Management:** By encouraging regular donors To return exactly when eligible, we maintain a consistent blood supply and avoid seasonal shortages.
    - **Platform Engagement:** Keeps the BloodMatch platform top-of-mind for donors, ensuring they are ready to respond when a critical emergency arises.
    - **Automation Efficiency:** Replaces the need for manual outreach by hospital staff to find eligible donors.

- **Logic:**
    1. Scan all donors in the database.
    2. Filter for those who have **never donated** OR whose **last donation was > 90 days ago**.
    3. Send a polite reminder email with donor guidelines to encourage them to visit a nearby blood bank or check the app for requests.

- **Manual Trigger for Demonstration (Viva):**
    Since we cannot wait until Monday morning for a demonstration, we provide a manual override:
    - **Endpoint:** `POST /api/test/notifications/trigger-eligibility`
    - **Role:** Immediately triggers the `EligibilityService` to scan for and notify all eligible donors, allowing for an instant showcase of the notification system in action.

### Case 2: Emergency donor Alerts (20km Radius)
**Goal:** Direct donor mobilization when blood banks cannot fulfill a critical request.
- **Trigger:** Handled by `EmergencyDonorAlertService` when a new `BloodRequest` is created and bank stock is insufficient.
- **Logic:**
    - **High Urgency:** The system first checks blood banks within a **20km radius**.
    - **Low Urgency:** The system checks **all blood banks** in the network.
    - **Fallback to Donors:** If **ZERO compatible stock** is found in the applicable banks:
        1. Identify eligible donors within a **20km radius** of the requesting hospital.
        2. Verify donor compatibility (considering both exact matches and compatible matches using `BloodCompatibilityUtil`).
        3. Verify eligibility (must be > 90 days since last donation).
        4. Send a high-urgency alert email (🔴 Red header for High Emergency) containing the hospital's specific address and the critical nature of the request.

---

## File-by-File Breakdown

| File Name | Role | Key Functionality |
| :--- | :--- | :--- |
| `WeeklyNotificationScheduler.java` | Task Scheduler | Triggers the weekly eligibility check every Monday at 10 AM using Cron. |
| `EmergencyDonorAlertService.java` | Alert Coordinator | Orchestrates the discovery of nearby donors during stock emergencies. |
| `EligibilityService.java` | Business Logic | Evaluates donor history against the 90-day cooldown period. |
| `NotificationManager.java` | Observer Subject | Concrete implementation of the Subject; manages the pipeline of notification delivery. |
| `EmailNotificationObserver.java` | Notification Dispatcher | Formats HTML templates and communicates with the SendGrid API. |
| `DistanceUtil.java` | Utility | Calculates geographic distance between coordinates to enforce the 20km radius. |
| `BloodCompatibilityUtil.java` | Domain Logic | Encapsulates the medical rules for blood group compatibility. |
| `NotificationTestController.java` | Manual Trigger Provider | Exposes REST endpoints (`/api/test/notifications/trigger-eligibility`) to demonstrate notifications instantly during evaluation. |
| `test_emergency_alert.sh` | Alert Simulation Script | A bash script that automates the creation of emergency requests to verify the end-to-end alert flow. |
| `application.properties` | Configuration | Stores SendGrid credentials and the primary sender email (Jagadeesh). |

---