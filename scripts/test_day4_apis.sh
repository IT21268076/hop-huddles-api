#!/bin/bash

BASE_URL="http://localhost:8080/api/v1"

echo "=== Day 4 Huddle Management API Testing ==="

# 1. Create sample data
echo "1. Creating Huddle Sample Data..."
SAMPLE_RESPONSE=$(curl -s -X POST $BASE_URL/test/create-huddle-sample-data)
echo "Sample data response: $SAMPLE_RESPONSE"

AGENCY_ID=$(echo $SAMPLE_RESPONSE | grep -o '"agencyId":[0-9]*' | cut -d':' -f2)
EDUCATOR_ID=$(echo $SAMPLE_RESPONSE | grep -o '"educatorId":[0-9]*' | cut -d':' -f2)
SEQUENCE_ID=$(echo $SAMPLE_RESPONSE | grep -o '"sequenceId":[0-9]*' | cut -d':' -f2)

echo "Agency ID: $AGENCY_ID, Educator ID: $EDUCATOR_ID, Sequence ID: $SEQUENCE_ID"

# 2. Sequence Management
echo "2. Testing Sequence Management..."

echo "Getting sequence by ID..."
curl -s $BASE_URL/sequences/$SEQUENCE_ID
echo

echo "Getting sequences by agency..."
curl -s $BASE_URL/sequences/agency/$AGENCY_ID
echo

echo "Creating new sequence..."
NEW_SEQUENCE_RESPONSE=$(curl -s -X POST "$BASE_URL/sequences?createdByUserId=$EDUCATOR_ID" \
  -H "Content-Type: application/json" \
  -d "{
    \"agencyId\": $AGENCY_ID,
    \"title\": \"Medication Management Training\",
    \"description\": \"Safe medication administration practices\",
    \"topic\": \"Medication safety and administration protocols\",
    \"estimatedDurationMinutes\": 60,
    \"targets\": [
      {\"targetType\": \"DISCIPLINE\", \"targetValue\": \"RN\"},
      {\"targetType\": \"ROLE\", \"targetValue\": \"FIELD_CLINICIAN\"}
    ]
  }")

NEW_SEQUENCE_ID=$(echo $NEW_SEQUENCE_RESPONSE | grep -o '"sequenceId":[0-9]*' | cut -d':' -f2)
echo "Created new sequence ID: $NEW_SEQUENCE_ID"

# 3. Huddle Management
echo "3. Testing Huddle Management..."

echo "Getting huddles by sequence..."
curl -s $BASE_URL/huddles/sequence/$SEQUENCE_ID
echo

echo "Creating new huddle..."
NEW_HUDDLE_RESPONSE=$(curl -s -X POST $BASE_URL/huddles \
  -H "Content-Type: application/json" \
  -d "{
    \"sequenceId\": $NEW_SEQUENCE_ID,
    \"title\": \"Medication Safety Basics\",
    \"huddleType\": \"INTRO\",
    \"durationMinutes\": 15,
    \"contentJson\": \"{\\\"intro\\\": \\\"Welcome to medication safety training\\\"}\",
    \"voiceScript\": \"Today we will learn about safe medication practices...\"
  }")

NEW_HUDDLE_ID=$(echo $NEW_HUDDLE_RESPONSE | grep -o '"huddleId":[0-9]*' | cut -d':' -f2)
echo "Created new huddle ID: $NEW_HUDDLE_ID"

echo "Updating huddle content..."
curl -s -X PUT $BASE_URL/huddles/$NEW_HUDDLE_ID/content \
  -H "Content-Type: application/json" \
  -d "{
    \"contentJson\": \"{\\\"sections\\\": [{\\\"title\\\": \\\"Safety First\\\", \\\"content\\\": \\\"Always verify patient identity and medication\\\"}]}\",
    \"voiceScript\": \"Safety is our top priority. Always verify patient identity before administering any medication...\"
  }"
echo

# 4. Target Management
echo "4. Testing Target Management..."

echo "Getting targets by sequence..."
curl -s $BASE_URL/sequence-targets/sequence/$SEQUENCE_ID
echo

echo "Adding new target..."
curl -s -X POST "$BASE_URL/sequence-targets?sequenceId=$NEW_SEQUENCE_ID&targetType=DISCIPLINE&targetValue=LPN"
echo

# 5. Sequence Status Updates
echo "5. Testing Sequence Status Management..."

echo "Moving sequence to REVIEW status..."
curl -s -X PUT "$BASE_URL/sequences/$NEW_SEQUENCE_ID/status?status=REVIEW&updatedByUserId=$EDUCATOR_ID"
echo

echo "Publishing sequence..."
curl -s -X POST "$BASE_URL/sequences/$NEW_SEQUENCE_ID/publish?publishedByUserId=$EDUCATOR_ID"
echo

# 6. Search and Filtering
echo "6. Testing Search and Filtering..."

echo "Searching sequences..."
curl -s "$BASE_URL/sequences/agency/$AGENCY_ID/search?title=Training&status=PUBLISHED&page=0&size=10"
echo

echo "Getting published sequences..."
curl -s $BASE_URL/sequences/agency/$AGENCY_ID/status/PUBLISHED
echo

# 7. Analytics
echo "7. Testing Analytics..."

echo "Getting sequence statistics..."
curl -s $BASE_URL/huddles/sequence/$SEQUENCE_ID/stats
echo

echo "Getting overall huddle statistics..."
curl -s $BASE_URL/test/huddle-stats
echo

echo "=== Day 4 API Testing Complete ==="
