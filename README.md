# BloodMatch

BloodMatch is a full-stack web application that connects **blood donors** with **hospitals** based on blood compatibility and geographical proximity. The system prioritizes urgent requests and efficiently notifies eligible donors.

---

## Tech Stack

- Backend: Spring Boot (Java)
- Frontend: React
- Database: MongoDB
- Build Tool: Maven
- Security: BCrypt Password Hashing

---

## Features

- User authentication (Donor / Hospital)
- Blood request creation and management
- Location-based donor matching
- Blood compatibility filtering
- Priority-based matching strategies
- Donor notification system
- Strategy pattern for flexible matching
- Observer pattern for notifications

---

## Architecture Overview

The system follows a **layered monolithic architecture**:

Controller → Service → Repository → Database

- Controller Layer: Handles HTTP requests and responses
- Service Layer: Contains business logic such as matching and validation
- Repository Layer: Handles database operations using MongoDB
- Database Layer: Stores application data

---

## Design Patterns Used

### Strategy Pattern:
- CompatibilityFirstStrategy: prioritizes exact blood group matches and proximity
- SmartMatchingStrategy: allows broader compatibility for urgent cases

### Observer Pattern:
- DonorNotifier: notifies matched donors when a request is created

---

## Architectural Tactics Used:

### Heartbeat:
- We run a scheduled health check every 10 seconds to test if different services (Donor, Hospital, BloodRequest, Matching) are working. 
- It marks each service as UP or DOWN based on whether their methods execute successfully. 
- The current status of all services can be viewed via the /health API endpoint.

### Authenticate Users:
- Donors and Hospital has to signup to use the application.
- After signing up they can login through their registered email and password.
- Routes like profile and raising blood requests are protected so only people who are logged in can access those.

### Maintain data confidentiality:
- Passwords of donors and hospitals are hashed before storing in database to support confidentiality.

### Built-in monitoring:
- Every service and controller has logs, so incase of failure we can find the point of failure by looking into logs

### Anticipate expected changes:
- The code implements Strategy pattern so if there are other strategies to find the nearest eligible donors we can add them easily by implementing the interface
- The same goes observer pattern (a new kind of notifying mechanism can be added)


## Setup Instructions:

### 1\. Clone the repository:

git clone https://github.com/Dedeepya396/BloodMatch.git  
cd BloodMatch  


### 2\. Backend Setup (Spring Boot)

Run the backend:

mvn spring-boot:run

OR run BloodmatchApplication.java from your IDE


### 3\. Frontend Setup (React)

cd bloodmatch-frontend  
npm install  
npm start  

### Configure MongoDB

Add your MongoDB connection details in  `src/main/resources/application.properties`