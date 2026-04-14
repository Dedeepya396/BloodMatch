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

Strategy Pattern:
- CompatibilityFirstStrategy: prioritizes exact blood group matches and proximity
- SmartMatchingStrategy: allows broader compatibility for urgent cases

Observer Pattern:
- DonorNotifier: notifies matched donors when a request is created

---

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