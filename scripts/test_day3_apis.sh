#!/bin/bash

BASE_URL="http://localhost:8080/api/v1"

echo "=== Day 3 API Testing ==="

# 1. Test Agency Creation
echo "1. Testing Agency Creation..."
AGENCY_RESPONSE=$(curl -s -X POST $BASE_URL/agencies \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Home Health Agency",
    "ccn": "654321",
    "agencyType": "HOME_HEALTH",
    "subscriptionPlan": "PREMIUM",
    "contactEmail": "contact@testhomehealth.com",
    "contactPhone": "555-9999",
    "address": "456 Test Street, Test City, TC 54321"
  }')

echo "Agency response: $AGENCY_RESPONSE"
AGENCY_ID=$(echo "$AGENCY_RESPONSE" | grep -o '"agencyId":[0-9]*' | cut -d':' -f2)
echo "Created Agency ID: $AGENCY_ID"

# 2. Test User Creation
echo "2. Testing User Creation..."

EDUCATOR_RESPONSE=$(curl -s -X POST $BASE_URL/users \
  -H "Content-Type: application/json" \
  -d '{
    "auth0Id": "auth0|test_educator_456",
    "email": "test.educator@testhomehealth.com",
    "name": "Test Educator",
    "phone": "555-8888"
  }')

echo "Educator response: $EDUCATOR_RESPONSE"
EDUCATOR_ID=$(echo "$EDUCATOR_RESPONSE" | grep -o '"userId":[0-9]*' | cut -d':' -f2)
echo "Created Educator ID: $EDUCATOR_ID"

LEARNER_RESPONSE=$(curl -s -X POST $BASE_URL/users \
  -H "Content-Type: application/json" \
  -d '{
    "auth0Id": "auth0|test_learner_456",
    "email": "test.learner@testhomehealth.com",
    "name": "Test Learner",
    "phone": "555-7777"
  }')

echo "Learner response: $LEARNER_RESPONSE"
LEARNER_ID=$(echo "$LEARNER_RESPONSE" | grep -o '"userId":[0-9]*' | cut -d':' -f2)
echo "Created Learner ID: $LEARNER_ID"

# 3. Test Assignment Creation
echo "3. Testing Assignment Creation..."

curl -s -X POST $BASE_URL/assignments \
  -H "Content-Type: application/json" \
  -d "{
    \"userId\": $EDUCATOR_ID,
    \"agencyId\": $AGENCY_ID,
    \"role\": \"EDUCATOR\",
    \"discipline\": \"RN\",
    \"isPrimary\": true
  }"

echo

curl -s -X POST $BASE_URL/assignments \
  -H "Content-Type: application/json" \
  -d "{
    \"userId\": $LEARNER_ID,
    \"agencyId\": $AGENCY_ID,
    \"role\": \"FIELD_CLINICIAN\",
    \"discipline\": \"RN\",
    \"isPrimary\": true
  }"

echo

# 4. Test Multi-tenant Queries
echo "4. Testing Multi-tenant Queries..."

echo "Users by Agency:"
curl -s $BASE_URL/users/agency/$AGENCY_ID
echo

echo "Assignments by Agency:"
curl -s $BASE_URL/assignments/agency/$AGENCY_ID
echo

echo "Assignments by Role (EDUCATOR):"
curl -s $BASE_URL/assignments/agency/$AGENCY_ID/role/EDUCATOR
echo

# 5. Test Search and Pagination
echo "5. Testing Search APIs..."

curl -s "$BASE_URL/agencies/search?name=Test&page=0&size=10"
echo

curl -s "$BASE_URL/users/agency/$AGENCY_ID/search?searchTerm=Test&page=0&size=10"
echo

echo "=== API Testing Complete ==="
