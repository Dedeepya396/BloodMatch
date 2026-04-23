import time
import requests
import statistics
import concurrent.futures
import os

# Configuration
URL = "http://localhost:8080/api/requests/match"
HEADERS = {"Content-Type": "application/json"}
PAYLOAD = {
    "hospitalName": "City Hospital",
    "bloodGroupRequired": "O+",
    "unitsRequired": 1,
    "urgency": "LOW",
    "latitude": 17.44,
    "longitude": 78.34
}

TOTAL_REQUESTS = 300
CONCURRENCY = 20

response_times = []

def calculate_dispersion(base_path, entity_name):
    directories = set()
    for root, dirs, files in os.walk(base_path):
        for file in files:
            if entity_name.lower() in file.lower() and file.endswith(".java"):
                directories.add(root)
    return len(directories)

def make_request():
    start = time.time()
    status_code = None
    try:
        # We enforce a timeout so it doesn't hang forever
        res = requests.post(URL, json=PAYLOAD, headers=HEADERS, timeout=10)
        status_code = res.status_code
    except Exception as e:
        status_code = 0
    end = time.time()
    return end - start, status_code

print("\n" + "="*50)
print(" BLOODMATCH AUTOMATED NFR QUANTIFICATION TOOL ")
print("="*50)
print(f"Targeting Endpoint : {URL}")
print(f"Total Requests     : {TOTAL_REQUESTS}")
print(f"Parallel Threads   : {CONCURRENCY}\n")

print("Sending simulated hospital requests... Please wait roughly 5-15 seconds...\n")

start_total = time.time()

# Launch a pool of parallel threads to bombard the server
success_count = 0
with concurrent.futures.ThreadPoolExecutor(max_workers=CONCURRENCY) as executor:
    futures = [executor.submit(make_request) for _ in range(TOTAL_REQUESTS)]
    for future in concurrent.futures.as_completed(futures):
        time_taken, status = future.result()
        response_times.append(time_taken)
        if status in (200, 201):
            success_count += 1

end_total = time.time()

# --- MATHEMATICAL CALCULATIONS ---
total_time = end_total - start_total
throughput_per_sec = TOTAL_REQUESTS / total_time
throughput_per_min = throughput_per_sec * 60

# Calculate P95 (95th percentile)
response_times.sort()
p95_index = int(len(response_times) * 0.95)
p95_value_ms = response_times[p95_index] * 1000

print("="*50)
print("                 FINAL RESULTS                  ")
print("="*50)

# --- NFR-01 Output ---
print(f"\n[ NFR-01: Performance — Response Time ]")
print(f"Goal   : Under 3000 milliseconds (< 3 seconds)")
print(f"Actual : {p95_value_ms:.2f} milliseconds (P95)")
if p95_value_ms < 3000:
    print(f"STATUS : ✅ PASS (Extremely Fast)")
else:
    print(f"STATUS : ❌ FAIL (Too Slow)")

# --- NFR-02 Output ---
print(f"\n[ NFR-02: Performance — Throughput ]")
print(f"Goal   : Greater than 200 requests per minute")
print(f"Actual : {throughput_per_min:.0f} requests per minute ({throughput_per_sec:.2f} req/sec)")
if throughput_per_min >= 200:
    print(f"STATUS : ✅ PASS (Sustained High Load)")
else:
    print(f"STATUS : ❌ FAIL (Throughput Too Low)")

# --- NFR-03 Output ---
error_rate = ((TOTAL_REQUESTS - success_count) / TOTAL_REQUESTS) * 100
print(f"\n[ NFR-03: Reliability — Error Rate ]")
print(f"Goal   : Under 1% Error Rate")
print(f"Actual : {error_rate:.2f}% ({TOTAL_REQUESTS - success_count} failed requests)")
if error_rate < 1:
    print(f"STATUS : ✅ PASS (Highly Reliable)")
else:
    print(f"STATUS : ❌ FAIL (Too Many Errors)")

# --- NFR-04 Output ---
layered_path = "../bloodmatch/src/main/java"
modular_path = "./src/main/java"
if os.path.exists(layered_path) and os.path.exists(modular_path):
    modules_to_check = ["Donor", "Hospital", "Request", "BloodBank"]
    
    layered_total = sum(calculate_dispersion(layered_path, m) for m in modules_to_check)
    modular_total = sum(calculate_dispersion(modular_path, m) for m in modules_to_check)
    
    print(f"\n[ NFR-04: Maintainability — Code Dispersion (Cohesion) ]")
    print(f"Goal   : Lower dispersion means higher architectural cohesion (less scattered)")
    print(f"Actual : Layered Architecture = {layered_total} total directories scattered across {len(modules_to_check)} core modules")
    print(f"       : Modular Architecture = {modular_total} total directories scattered across {len(modules_to_check)} core modules")
    if modular_total < layered_total:
        print(f"STATUS : ✅ PASS (Modular Monolith reduces total scatter by {layered_total - modular_total} folders!)")
    else:
        print(f"STATUS : ❌ FAIL (No maintainability improvement measured)")

print("\n" + "="*50)
print("Take a screenshot of these metrics to include in your academic report!")
print("="*50 + "\n")
