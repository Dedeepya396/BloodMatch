# Non-Functional Requirements (NFR) Quantification Methodology

This document outlines the systematic approaches used to mathematically quantify the Non-Functional Requirements (NFRs) for the BloodMatch platform. Our goal is to provide a highly reproducible metric framework that explicitly contrasts the resilience and structure of the original Layered architecture against the newly implemented Modular Monolith architecture.

We utilized an automated, lightweight Python-based load testing and static analysis script (`quantify_nfrs.py`) to systematically measure these metrics.

---

## 1. NFR-01: Performance — Response Time (P95)

**Goal:** Guarantee that the core blood-matching algorithmic endpoint returns responses in under 3 seconds (3000 ms).

**Methodology:**
- **Technique:** Automated API Load Testing.
- **How it's done:** The automated script triggers exactly 300 POST requests targeting the main algorithmic engine (`/api/requests/match`). The start execution time and completion time (Unix timestamps) are recorded internally for every individual network request to accurately capture distinct request durations.
- **Metric Evaluation:** Instead of using a simple mathematical average—which hides massive, unacceptable latency latency spikes—we calculate the **95th Percentile (P95)**. All response durations are ordered from fastest to slowest, and we draw the value precisely at the 95% threshold.
- **Why it matters:** P95 is the strict industry standard for modern SLAs constraint measuring. Utilizing P95 guarantees mathematical proof that 95% of field users (Hospitals) receive their critical blood matches efficiently, completely preventing uncharacteristically fast outlier requests from skewing the perceived health of the system.

---

## 2. NFR-02: Performance — Throughput

**Goal:** The system scales to support sustained heavy processing workloads (e.g., > 200 requests/minute).

**Methodology:**
- **Technique:** High-Concurrency Stress Testing.
- **How it's done:** We utilize Python's `concurrent.futures.ThreadPoolExecutor` to instantiate 20 parallel processing threads. These threads bombard the Spring Boot instance synchronously, acting like a traffic surge occurring during a major metropolitan emergency.
- **Metric Evaluation:** The absolute aggregate time is captured for when the first request begins until the 300th request resolves. The calculation `(Total Requests / Total Time in Seconds) * 60` calculates the pure per-minute data capability (Throughput).
- **Why it matters:** In the real world, system engines generally do not fail under serialized sequential loads. They break under multithreaded synchronization locks. Generating massive concurrent state traffic definitively checks backend connection locking efficiency and validates that overall system architecture isn’t fundamentally bottlenecked by concurrent DB connections.

---

## 3. NFR-03: Reliability — Error Rate

**Goal:** Ensure that the API remains highly resilient (< 1% error rate) when subjected directly to density traffic storms.

**Methodology:**
- **Technique:** Structural Logic Validation.
- **How it's done:** Inside the parallel threading bombardment phase, exact HTTP status codes returned by the Spring backend are analyzed in addition to the timing parameters.
- **Metric Evaluation:** The script actively mandates the server responds with success blocks (`HTTP 200 OK` or `201 Created`). The numeric formula `((Total Requests - Success Count) / Total Requests) * 100` converts API failures into a pure architectural percentage.
- **Why it matters:** Excellent Response Time and Throughput measurements are totally worthless if the backend handles that load by aggressively dropping connections with `500 Internal Server Errors`. Natively locking the error-rate checks into the concurrent load suite mathematically proves that the business logic safely resolves data collisions globally.

---

## 4. NFR-04: Maintainability — Code Dispersion (Cohesion)

**Goal:** Prove structurally that modifying or isolating specific business capabilities is vastly simpler in the Modular Monolith than the original design.

**Methodology:**
- **Technique:** Automated Source Code Static Analysis.
- **How it's done:** The quantification engine harnesses Python’s `os.walk` to natively crawl the real `/src/main/java` nested directories of *both* the original `bloodmatch` codebase and the newly architecturally refactored `bloodmatch-modular` project.
- **Metric Evaluation:** The script tracks four primary system domains simultaneously: `["Donor", "Hospital", "Request", "BloodBank"]`. It counts the absolute number of distinct architectural directories across which these domains scatter their files.
- **Why it matters:** Modern software engineering theory strictly correlates poor maintainability (low cohesion) to severe “Feature Scattering” (e.g., where adding a single field requires modifying 6 completely different folders). By statically measuring the physical directory footprint scale between the two, this analysis provides an impenetrable, empirical artifact that shifting to a Modular paradigm structurally curtails code fragmentation and enforces decoupled business logic rules.
