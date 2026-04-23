# NFR Analysis and Architectural Trade-offs

To properly evaluate our architectural migration, we compared the original Layered architecture with our new Modular Monolith. We used an automated Python script to simulate heavy load (300 concurrent requests from 20 parallel users) and tracked how both systems performed. We also wrote a quick static analysis function to count how scattered the code was across folders to measure maintainability. 

Here are the results:

### 1. The Numbers

**Layered Monolith (Original system):**
* **Response Time (P95):** 2945.57 ms 
* **Throughput:** 483 requests/minute (8.06 req/sec)
* **Error Rate:** 0.00% under load
* **Maintainability:** Code for core modules was scattered across 20 different folders.

**Modular Monolith (New system):**
* **Response Time (P95):** 2981.33 ms 
* **Throughput:** 468 requests/minute (7.80 req/sec)
* **Error Rate:** 0.00% under load
* **Maintainability:** Code for core modules is grouped into just 9 feature-specific folders.

---

### 2. Analysis of the Results

When looking at the performance metrics (Response Time and Throughput), the original layered architecture was technically slightly faster. It handled about 15 more requests per minute and returned responses roughly 35 milliseconds faster. However, in the grand scheme of a web application dealing with network latency and database calls, a 1-3% difference in performance is basically negligible. Both architectures completely passed the performance goals set in the NFRs without throwing any errors.

The real story here is in NFR-4 (Maintainability). By shifting to a modular design, we cut the "code scatter" by more than half (from 20 folders down to 9). In the old system, if you wanted to understand how "Donors" worked, you had to jump between the Controller, Service, Repository, Model, and DTO folders. Now, practically everything you need is right inside the `donor` module folder. 

---

### 3. Understanding the Trade-offs

Moving to a modular monolith wasn't a silver bullet; we had to make some conscious trade-offs along the way.

**Trade-off 1: Slight Performance Drop vs. Better Encapsulation**
The modular monolith is technically slightly slower because we introduced strict boundaries. Before, one service could just directly query another domain's database repository. It was fast, but it led to tight coupling. Now, modules are forced to talk to each other through proper service interfaces. We traded a tiny bit of execution speed to ensure our modules remain decoupled and easier to manage.

**Trade-off 2: Upfront Setup Complexity vs. Long-term Developer Sanity**
Setting up the modular architecture took more upfront thought. We had to implement patterns like the Observer pattern for notifications and figure out how to pass messages between completely separate modules without entangling them. However, this protects the codebase long-term. Currently, a developer working on the `request` module physically can't easily break internal logic in the `donor` module because those internal classes aren't exposed.

**Trade-off 3: Extra Code (Boilerplate) vs. Microservice Readiness**
Because modules shouldn't share database entities directly anymore, we wrote more Data Transfer Objects (DTOs) to pass information between boundaries safely. This meant writing mapping code and having more files overall. The payoff, though, is that the system is now essentially "microservice-ready." If the matching algorithm suddenly needed its own dedicated server to handle a massive spike in concurrent hospitals, we could safely extract the `request` module entirely because it doesn't share hidden dependencies with the rest of the application.

**Trade-off 4: Team Independence vs. Global Standards**
In the old layered architecture, developers basically worked across the entire stack. In a modular monolith, you generally assign specific teams to own specific modules (like a "Request Team" and a "Donor Team"). This is great for independence—the Request team can move fast without waiting on anyone. The trade-off is that you might lose some global code uniformity because teams might solve internal problems differently within their own isolated modules.

**Trade-off 5: Strict Data Boundaries vs. Easy Database Queries**
A true modular monolith isolates data so that modules don't directly access each other's database tables. Before, you could write a single fast SQL `JOIN` or MongoDB `$lookup` to grab a hospital and its blood requests together. Now, you have to query the hospital module for the hospital data, and then ask the request module for the blood requests. We traded the sheer convenience (and speed) of cross-table database queries for absolute data sovereignty—meaning a bad database query in one module can't accidentally lock or corrupt another module's database tables.

**Trade-off 6: Fast Local Testing vs. Complex Integration Testing**
Testing the old layered monolith usually meant we had to spin up the entire application context at once to properly test how the controllers talked down to the services and database. By going modular, we can now write extremely fast, targeted unit tests for just the `donor` module in total isolation. However, the trade-off is that because the modules are heavily separated, we now have to write slightly more complex "Integration Tests" to prove that the different modules actually talk to each other correctly when fully assembled.
