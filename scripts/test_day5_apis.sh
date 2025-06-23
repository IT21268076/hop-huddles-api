#!/bin/bash

BASE_URL="http://localhost:8080/api/v1"

echo "=== Day 5 Progress Tracking & Analytics API Testing ==="

# Create sample progress data first
echo "1. Creating Progress Sample Data..."
SAMPLE_RESPONSE=$(curl -s -X POST "$BASE_URL/test/create-progress-sample-data")
echo "Sample data response: $SAMPLE_RESPONSE"
echo

LEARNER_ID=$(echo "$SAMPLE_RESPONSE" | grep -o '"learnerId":[0-9]*' | cut -d':' -f2)
SEQUENCE_ID=$(echo "$SAMPLE_RESPONSE" | grep -o '"sequenceId":[0-9]*' | cut -d':' -f2)

echo "Learner ID: $LEARNER_ID"
echo "Sequence ID: $SEQUENCE_ID"
echo

# Test Progress APIs
echo "2. Testing Progress Management..."

echo "Getting user progress..."
curl -s "$BASE_URL/progress/user/$LEARNER_ID"
echo -e "\n"

echo "Getting sequence progress..."
curl -s "$BASE_URL/sequence-progress/sequence/$SEQUENCE_ID"
echo -e "\n"

echo "Starting new huddle..."
curl -s -X POST "$BASE_URL/progress/start?userId=$LEARNER_ID&huddleId=3"
echo -e "\n"

echo "Updating progress..."
curl -s -X PUT "$BASE_URL/progress/update" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": '"$LEARNER_ID"',
    "huddleId": 3,
    "completionPercentage": 75.50,
    "timeSpentMinutes": 10.25
  }'
echo -e "\n"

echo "Recording assessment..."
curl -s -X POST "$BASE_URL/progress/assessment?userId=$LEARNER_ID&huddleId=3&score=92.5"
echo -e "\n"

# Test Analytics APIs
echo "3. Testing Analytics..."

echo "Getting agency analytics..."
curl -s "$BASE_URL/analytics/agency/1"
echo -e "\n"

echo "Getting sequence analytics..."
curl -s "$BASE_URL/analytics/sequence/$SEQUENCE_ID"
echo -e "\n"

echo "Getting user analytics..."
curl -s "$BASE_URL/analytics/user/$LEARNER_ID"
echo -e "\n"

# Test Engagement APIs
echo "4. Testing Engagement Tracking..."

echo "Recording view event..."
curl -s -X POST "$BASE_URL/engagement/huddle?userId=$LEARNER_ID&huddleId=1&eventType=VIEW&sessionId=test_session_123"
echo -e "\n"

echo "Recording download event..."
curl -s -X POST "$BASE_URL/engagement/huddle?userId=$LEARNER_ID&huddleId=1&eventType=DOWNLOAD&sessionId=test_session_123"
echo -e "\n"

echo "Getting user engagement history..."
curl -s "$BASE_URL/engagement/user/$LEARNER_ID"
echo -e "\n"

# Test Statistics
echo "5. Testing Statistics..."

echo "Getting progress statistics..."
curl -s "$BASE_URL/test/progress-stats"
echo -e "\n"

echo "=== Day 5 API Testing Complete ==="
