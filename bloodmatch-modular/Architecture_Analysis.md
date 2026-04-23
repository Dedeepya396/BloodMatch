# Architecture Analysis: Layered Monolith vs. Modular Monolith & NFR Quantification

## 1. Architectural Comparison

Our prototype migrated the BloodMatch system from a traditional **Layered Monolith** to a **Modular Monolith**. 

### The Trade-offs

| Feature | Layered Monolith (Previous) | Modular Monolith (Implemented) | Trade-off Discussion |
| :--- | :--- | :--- | :--- |
| **Logic Grouping** | **Technical:** Clustered by roles (Controllers, Services, Models). | **Business Capability:** Clustered by features (Donor, Hospital, Request). | The modular monolith forces developers to keep related business logic together. Finding all logic related to "Hospitals" takes 1 second natively, completely solving code-scatter issues. |
| **Encapsulation / Coupling**| **High Coupling:** The matching engine could directly query the Donor database tables. | **Strictly Encapsulated:** The Request matching engine must ask the `DonorService` interface to get Donors. | By forcing modules to use public interfaces to talk to each other, changing the database schema of the Donor table no longer breaks the algorithmic Matching Engine. |
| **Migration to Microservices**| **Extremely Hard:** Because data and logic overlap everywhere, ripping out a piece is a nightmare. | **Extremely Easy:** The `notification` module can be ripped out tomorrow and deployed as an AWS Lambda because it is 100% self-contained. | We sacrificed the rapid early-stage prototyping speed of a Layered Monolith to gain incredible long-term scalability and code maintainability. |

---

## 2. Which NFRs Are Easiest to Quantify?

Looking at your NFR list, **NFR-01 (Response Time)** and **NFR-02 (Throughput)** are by far the absolutely easiest to quantify right now on your local machine.

*   You cannot easily quantify **NFR-03 (Availability)** because that requires tracking production uptime over months.
*   You cannot easily quantify **NFR-04 (Scalability)** because scaling from 100,000 to 1,000,000 donors requires generating a massive dummy database and provisioning AWS clusters.
*   But **Performance (Time and Throughput)** can be strictly quantified mathematically in 5 minutes using industry-standard load-testing software against your local `http://localhost:8080/api/requests/match` endpoint!

---

## 3. EXACT GUIDE: How to Quantify NFR-01 & NFR-02 using Apache JMeter

According to your document, you must measure Response Time (NFR-01) via P95 percentiles using tools like **Apache JMeter**, and Throughput (NFR-02) via stress testing. 

Here is exactly what you need to do, step-by-step, to officially quantify these metrics for your report.

### Step A: Setup JMeter
1. Download **Apache JMeter** from `jmeter.apache.org` and extract the zip file.
2. Open the `bin` folder and double-click `jmeter.bat` (Windows) or run `./jmeter` (Mac/Linux) to open the GUI.
3. Make sure your Spring Boot backend (`mvn spring-boot:run`) is currently running in the background.

### Step B: Configure the Load Test (Thread Group)
This simulates NFR-02's "gradually increasing request rates" and multiple users.
1. In JMeter, right-click **Test Plan** > **Add** > **Threads (Users)** > **Thread Group**.
2. **Name it:** "BloodMatch Stress Test"
3. Set **Number of Threads (users):** to `100` (This simulates 100 concurrent hospitals requesting blood at the exact same time).
4. Set **Ramp-up period (seconds):** to `10` (This means 10 new users are added every second, fulfilling the "graduating increase" requirement).
5. Set **Loop Count:** to `10` (This means each user will ask for blood 10 times consecutively).

### Step C: Configure the API Request
This tells JMeter exactly what endpoint to shoot at to test the core blood matching algorithm.
1. Right-click your new **Thread Group** > **Add** > **Sampler** > **HTTP Request**.
2. Change the Method to **POST**.
3. In **Server Name or IP**, type: `localhost`
4. In **Port Number**, type: `8080`
5. In **Path**, type: `/api/requests/match`
6. Click the **Body Data** tab at the bottom and paste a dummy payload:
   ```json
   {
       "hospitalName": "Apollo Hospital",
       "bloodGroupRequired": "O+",
       "unitsRequired": 2,
       "urgency": "HIGH",
       "latitude": 17.44,
       "longitude": 78.34
   }
   ```
7. *Important:* Right-click **Thread Group** > **Add** > **Config Element** > **HTTP Header Manager**. Click "Add" at the bottom. Under Name put `Content-Type` and under Value put `application/json`. (This ensures the Spring Boot server accepts the JSON payload).

### Step D: Add the Measurement Dashboards
This is where the actual *Quantification* happens!
1. Right-click **Thread Group** > **Add** > **Listener** > **View Results Tree** (Shows you if requests succeed or fail).
2. Right-click **Thread Group** > **Add** > **Listener** > **Aggregate Report** (This is the most important one! It generates your mathematical proofs!).

### Step E: Run the Test and Read the Data
1. Click the green "Play" button at the top of JMeter to start hammering the Modular Monolith endpoint.
2. Click on the **Aggregate Report** on the left menu. Once the test finishes, you will see a spreadsheet. 

### How to use this data for your documentation:
*   **To prove NFR-01 (Response Time: max 2 seconds):** Look at the column labeled **`95% Line`**. This is your exactly required P95 metric! This number is in milliseconds. If it says `125`, this proves your Response Time P95 is just `0.125 seconds`, easily wiping out the 2-second limit requirement! Take a screenshot of this row.
*   **To prove NFR-02 (Throughput: >200 per min):** Look at the column labeled **`Throughput`**. It will say something like `25.5/sec`. Multiply that by 60 (25.5 * 60 = 1530 requests per minute). Since 1530 > 200, you have just mathematically quantified and proved that your system survives the stress test requirement. Take a screenshot of this cell.

You now have a literal framework and raw data method to perfectly prove your two NFRs!
