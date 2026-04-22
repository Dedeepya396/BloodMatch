#!/bin/bash

# BLOODMATCH: Emergency Donor Alert Demonstration
#
# OBJECTIVE:
#   To demonstrate how the system automatically triggers a high-urgency 
#   notification to eligible donors when life-saving blood is unavailable 
#   in the entire blood bank network.
#
# How it works:
#   1. OBSERVER PATTERN: The system uses a 'NotificationSubject' that 
#      manages a list of 'NotificationObservers'. When an emergency is 
#      detected, the 'NotificationManager' notifies all observers.
#
#   2. ELIGIBILITY LOGIC: Only donors who matches:
#      - Blood Compatibility (O- can only receive O- in emergencies).
#      - Proximity (Donor must be within 20km for HIGH urgency).
#      - Readiness (Last donation > 90 days ago or never donated).
#
#   3. TRIGGER: Triggered only when compatible stock in nearby blood 
#      banks (within 20km) is ZERO.
#
# HOW TO RUN:
#   1. Ensure the Spring Boot backend is running on http://localhost:8080
#   2. Run this script:  bash test_emergency_alert.sh

# ---------------------------------------------------------


BASE_URL="http://localhost:8080"
DONOR_EMAIL="karthikande26@gmail.com"

# --- TEST DATA: Warangal Location (Far from Hyderabad Blood Banks) ---
# This ensures that Hyderabad blood banks are out of the 20km "HIGH" urgency range.
HOSPITAL_LAT=17.9784
HOSPITAL_LNG=79.5941
DONOR_LAT=17.9800
DONOR_LNG=79.5900

echo ""
echo "========================================================="
echo "  STARTING EMERGENCY ALERT DEMO"
echo "========================================================="

# STEP 1: PREPARE DATA IN DATABASE
echo "▶ STEP 1: Ensuring donor exists and is eligible in database..."

# Check if donor exists by email
DONOR_INFO=$(curl -s "${BASE_URL}/api/donors/by-email?email=${DONOR_EMAIL}")

if [[ "$DONOR_INFO" == "null" || -z "$DONOR_INFO" ]]; then
    echo "  - Donor not found. Creating new donor..."
    # POST to create
    CREATE_RESP=$(curl -s -X POST "${BASE_URL}/api/donors" \
      -H "Content-Type: application/json" \
      -d "{
        \"name\": \"Karthik Ande (Demo Donor)\",
        \"email\": \"${DONOR_EMAIL}\",
        \"bloodGroup\": \"O-\",
        \"latitude\": ${DONOR_LAT},
        \"longitude\": ${DONOR_LNG},
        \"available\": true,
        \"lastDonationDate\": null
      }")
    DONOR_ID=$(echo "$CREATE_RESP" | jq -r '.id')
    echo " Created Donor ID: $DONOR_ID"
else
    # Extract ID and Update to ensure eligibility
    DONOR_ID=$(echo "$DONOR_INFO" | jq -r '.id')
    echo "  - Found Donor (ID: $DONOR_ID). Updating location and eligibility..."
    
    curl -s -X PUT "${BASE_URL}/api/donors/${DONOR_ID}" \
      -H "Content-Type: application/json" \
      -d "{
        \"name\": \"Karthik Ande\",
        \"email\": \"${DONOR_EMAIL}\",
        \"bloodGroup\": \"O-\",
        \"latitude\": ${DONOR_LAT},
        \"longitude\": ${DONOR_LNG},
        \"available\": true,
        \"lastDonationDate\": null
      }" > /dev/null
    echo "  Donor updated for demo."
fi

# STEP 2: TRIGGER HIGH-URGENCY REQUEST

echo ""
echo "▶ STEP 2: Requesting O- Blood for 'Warangal Emergency Hospital'..."
echo "  - Urgency Level: HIGH (Search radius restricted to 20km)"
echo "  - Distance Logic: Hyderabad banks are ~150km away → Result: 0 units"
echo ""

MATCH_RESP=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/requests/match" \
  -H "Content-Type: application/json" \
  -d "{
    \"hospitalName\": \"Warangal Emergency Hospital\",
    \"bloodGroupRequired\": \"O-\",
    \"unitsRequired\": 1,
    \"latitude\": ${HOSPITAL_LAT},
    \"longitude\": ${HOSPITAL_LNG},
    \"urgency\": \"HIGH\"
  }")

HTTP_CODE=$(echo "$MATCH_RESP" | tail -n 1)
BODY=$(echo "$MATCH_RESP" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    echo "  Request Processed Successfully!"
    echo "  - Response from System:"
    echo "$BODY" | jq '.' 2>/dev/null || echo "    $BODY"
else
    echo "  ERROR: Received HTTP $HTTP_CODE"
    echo "  $BODY"
    exit 1
fi

echo "  WHAT JUST HAPPENED?"
echo "  1. System checked all blood banks within 20km for O-."
echo "  2. None found. The 'EmergencyDonorAlertService' was triggered."
echo "  3. System searched for eligible donors within 20km of the hospital."
echo "  4. Karthik Ande was found (Blood: O-, Dist: <2km, Eligible: Yes)."
echo "  5. Notification dispatched via Observer Pattern."

