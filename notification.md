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

#### A. Availability (Fault Detection & Recovery)
- **Exception Handling (Fault Detection):** The `EmailNotificationObserver` uses robust `try-catch` blocks to detect and log communication failures with the SendGrid API. This prevents a single failed email from crashing the entire notification pipeline.
- **Graceful Degradation:** If the email service is down, the system logs the error but allows other observers (like future SMS or loggers) to continue their work.

#### B. Performance (Resource Management)
- **Scheduling Policy (Resource Management):** We use Spring's `@Scheduled` annotation with **Cron expressions** to manage the event rate for weekly notifications. This ensures background tasks run during off-peak hours (Monday 10 AM) without impacting real-time user requests.
- **Efficient Filtering:** Proximity-based filtering (Haversine formula) is used to bound the processing demand by only targeting donors within a 20km radius, rather than scanning the entire global database for every alert.

#### C. Security (Resisting Attacks)
- **Limit Exposure:** Sensitive credentials like the `sendgrid.api-key` are never hardcoded. They are managed through `application.properties` and environment variables, ensuring that if the source code is exposed, the API keys remain secure.
- **Input Validation:** All notification content is sanitized and validated in `NotificationContentUtil` before being dispatched to prevent injection attacks.

#### D. Modifiability (Localize Changes & Prevent Ripple Effects)
- **Use an Intermediary:** The `NotificationManager` acts as an intermediary (Subject) between the business logic and the delivery observers. This prevents business services from needing to know about email protocols.
- **Generalize Module:** The use of the **Observer Pattern** generalizes the notification delivery system. Adding a new channel (e.g., SMS) only requires adding a new observer class, localizing all changes to that single module.
- **Maintain Existing Interfaces:** By adhering to the `NotificationObserver` interface, we ensure that changes in one observer do not ripple through the rest of the system.

#### E. Testability (Manage Input/Output)
- **Separate Interface from Implementation:** We use interfaces (`NotificationObserver`, `NotificationSubject`) to separate the contract from the actual logic. This allows us to "Specialize Access" during testing by using mock observers to verify notification triggers without sending actual emails.

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