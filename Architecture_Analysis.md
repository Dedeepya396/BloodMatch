# Architecture Analysis: Layered Monolith vs. Modular Monolith

## 1. Architectural Transformation Approach
We successfully refactored the BloodMatch application from a **Layered Monolithic Architecture** to a **Modular Monolithic Architecture**. 

### What was done:
1. **Preserved Original Codebase:** The original Layered code is untouched in the `bloodmatch/` directory. We created a duplicate directory `bloodmatch-modular/`.
2. **Feature-based Packaging:** We reorganized the package structure from technical layers (e.g., `controller`, `service`, `repository`, `model`) into cohesive business domains:
   - `auth`: Security, User entities, authentication logic.
   - `bloodbank`: Blood banks, packets, storage, and bank requests.
   - `donor`: Donor registration, alerts, eligibility, and updating logic.
   - `hospital`: Hospital profiles and hospital request status.
   - `request`: Blood requests, matching logic, allocation routing.
   - `notification`: Notification observers and managers.
   - `core`: Shared heartbeat/health checkpoints, CORS configurations, strategies (Smart Matching), and Utils.
3. **Encapsulation:** By placing the controller, service, repository, and models in a single module umbrella (e.g., `com.example.bloodmatch.request`), we've decoupled domains. Communication between domains (like `MatchingService` needing access to `BloodBank`) must happen through distinct service interfaces rather than directly pulling from another domain's repository.

## 2. Comparison and Trade-offs (Layered vs. Modular)

| Feature | Layered Monolith | Modular Monolith |
|---|---|---|
| **Organization** | Grouped by technical concern (Controllers in one place, Repositories in another). | Grouped by business capability/domain. |
| **Cognitive Load** | High for adding features; a developer has to modify files across multiple folders just to add one feature (e.g. `Donor`). | Low; a developer primarily works within a single feature module (`donor/`). |
| **Coupling** | High cross-domain coupling. It's easy for `BloodRequestService` to directly query `DonorRepository`, breaking boundaries. | Low cross-domain coupling. `BloodRequestService` is forced to call `DonorService`, encapsulating data access properly. |
| **Microservices Readiness** | Very low. Ripping out a feature into a microservice requires unweaving heavily tangled technical layers. | Very high. A domain module (e.g., `notification`) can easily be extracted into an independent microservice later since its dependencies are localized. |
| **Deployment & DB** | Single process footprint, single DB. | Single process footprint, single DB. |

## 3. Plan to Quantify Non-Functional Requirements (NFRs)
The objective is to quantify **Response Time** and **Throughput** for the two architectures. Because they are both run on a single JVM and use in-memory function calls, the raw performance metrics should be nearly identical. However, to formally verify this and quantify the NFRs, we follow this testing plan:

### NFR 1: Response Time (Latency)
**Definition:** The time it takes for the application to process the `/api/requests/match` endpoint (which maps the `MatchingService` and cross-module calls) and return a 200 OK.
* **Testing Tool:** Apache JMeter or Postman Collection Automation.
* **Quantification Steps:**
  1. Boot up the original `bloodmatch` backend. Hit the match endpoint 1,000 times sequentially.
  2. Record the 95th Percentile (p95) and 99th Percentile (p99) response latency in milliseconds.
  3. Boot up the refactored `bloodmatch-modular` backend. Repeat the 1,000 sequential API requests.
  4. Compare the median and p99 response times. Since no external network hops were introduced (unlike microservices), the deviation should be strictly < 2%.

### NFR 2: Throughput (Requests Per Second)
**Definition:** The maximum number of successful matching requests the application can handle per second before exhausting threads or memory.
* **Testing Tool:** `wrk` benchmarking tool or JMeter.
* **Quantification Steps:**
  1. Boot up the original `bloodmatch` application.
  2. Run `wrk -t10 -c100 -d30s http://localhost:8080/api/requests/match` (simulate 100 concurrent hospital requests).
  3. Record the "Requests/sec" output metric.
  4. Repeat the test precisely on `bloodmatch-modular`.
  5. Compare the Throughput values. A well-organized modular structure with clean interface bounds should result in the same JVM throughput capabilities (expected identical TPS constraints relative to CPU bounds). 

### What to do next to execute this quantification:
1. Make sure Java 21 environment is active on your testing machine (as the app targets Java 21 / Spring Boot 3+).
2. Start MongoDB instance locally so the application runs uninterrupted.
3. Install Apache JMeter or `wrk` via `sudo apt install wrk`.
4. Define a standard JSON payload representing a hospital blood request and feed it into the `wrk` script for the benchmarking to begin.
