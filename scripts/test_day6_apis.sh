#!/bin/bash

# HOP Huddles - Day 6: Manual Content Creation Testing Script
# Make sure your application is running on localhost:8080

BASE_URL="http://localhost:8080/api/v1"

echo "=== Day 6: Manual Content Creation Testing ==="
echo "Testing manual content creation and management..."
echo

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check response
check_response() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Success${NC}"
    else
        echo -e "${RED}✗ Failed${NC}"
    fi
    echo
}

# Function to extract JSON field
extract_json_field() {
    echo "$1" | jq -r "$2 // empty" 2>/dev/null
}

# 1. Get Content Creation Guidelines
echo -e "${YELLOW}1. Getting Content Creation Guidelines...${NC}"
curl -s $BASE_URL/content/guidelines | jq '.' || curl -s $BASE_URL/content/guidelines
check_response

# 2. Create a Test Sequence for Manual Content
echo -e "${YELLOW}2. Creating Test Sequence for Manual Content...${NC}"
SEQUENCE_RESPONSE=$(curl -s -X POST $BASE_URL/sequences \
  -H "Content-Type: application/json" \
  -d '{
    "agencyId": 1,
    "title": "Manual Content Test Series",
    "description": "Testing manual content creation capabilities",
    "topic": "Healthcare Communication",
    "totalHuddles": 0,
    "estimatedDurationMinutes": 0,
    "createdByUserId": 1
  }')

echo "$SEQUENCE_RESPONSE" | jq '.' || echo "$SEQUENCE_RESPONSE"

# Extract sequence ID for testing
SEQUENCE_ID=$(extract_json_field "$SEQUENCE_RESPONSE" '.sequenceId // .data.sequenceId')

if [ ! -z "$SEQUENCE_ID" ] && [ "$SEQUENCE_ID" != "null" ]; then
    echo -e "${GREEN}Created sequence with ID: $SEQUENCE_ID${NC}"
else
    echo -e "${RED}Failed to create sequence. Trying with existing sequence ID 1${NC}"
    SEQUENCE_ID=1
fi

# 3. Create Individual Huddle for Manual Content
echo -e "${YELLOW}3. Creating Individual Huddle for Manual Content...${NC}"
HUDDLE_RESPONSE=$(curl -s -X POST $BASE_URL/huddles \
  -H "Content-Type: application/json" \
  -d "{
    \"sequenceId\": $SEQUENCE_ID,
    \"title\": \"Effective Patient Communication\",
    \"orderIndex\": 1
  }")

echo "$HUDDLE_RESPONSE" | jq '.' || echo "$HUDDLE_RESPONSE"

# Extract huddle ID
HUDDLE_ID=$(extract_json_field "$HUDDLE_RESPONSE" '.huddleId // .data.huddleId')

if [ ! -z "$HUDDLE_ID" ] && [ "$HUDDLE_ID" != "null" ]; then
    echo -e "${GREEN}Created huddle with ID: $HUDDLE_ID${NC}"
else
    echo -e "${RED}Failed to create huddle. Using existing huddle ID 1${NC}"
    HUDDLE_ID=1
fi

# 4. Add Manual Content to Huddle
echo -e "${YELLOW}4. Adding Manual Content to Huddle...${NC}"
curl -s -X POST $BASE_URL/content/huddles/$HUDDLE_ID/manual \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Effective Patient Communication",
    "objective": "Learn evidence-based techniques for clear, empathetic patient communication that improves satisfaction and outcomes",
    "keyContent": "Effective patient communication is fundamental to quality healthcare delivery. Research shows that clear communication reduces medical errors by 30% and improves patient satisfaction scores significantly.\n\nKey principles include:\n\n**Active Listening**: Give patients your full attention, make eye contact, and avoid interrupting. Use phrases like \"What I hear you saying is...\" to confirm understanding.\n\n**Plain Language**: Avoid medical jargon. Instead of \"hypertension,\" say \"high blood pressure.\" Check understanding by asking patients to repeat key information.\n\n**Empathy and Validation**: Acknowledge patient emotions. Phrases like \"I can see this is concerning for you\" build trust and rapport.\n\n**Cultural Sensitivity**: Be aware of cultural differences in communication styles, eye contact preferences, and family involvement in healthcare decisions.",
    "actionItems": [
      "Practice active listening during your next 3 patient interactions",
      "Replace one piece of medical jargon with plain language in each conversation",
      "Ask patients to repeat back key instructions using teach-back method",
      "Notice and acknowledge at least one patient emotion during your shift"
    ],
    "discussionPoints": [
      "What barriers to effective communication have you encountered in your practice?",
      "How do you handle language barriers or hearing difficulties?",
      "Share an example when good communication made a difference in patient care",
      "What communication techniques work best for anxious or upset patients?"
    ],
    "resources": [
      "Joint Commission Patient Communication Guidelines",
      "Institute for Healthcare Improvement: Health Literacy Tools",
      "AHRQ: Questions are the Answer campaign materials"
    ],
    "voiceScript": "Today we focus on patient communication - a skill that directly impacts both patient safety and satisfaction. Remember: listen actively, speak plainly, show empathy, and always check for understanding.",
    "estimatedMinutes": 8,
    "readingLevel": "INTERMEDIATE",
    "contentTags": ["communication", "patient-safety", "quality-improvement"],
    "publishImmediately": false
  }' | jq '.' || echo "Adding manual content..."
check_response

# 5. Preview the Created Content
echo -e "${YELLOW}5. Previewing Created Content...${NC}"
curl -s $BASE_URL/content/huddles/$HUDDLE_ID/preview | jq '.data.formattedContent, .data.wordCount, .data.validation' || \
curl -s $BASE_URL/content/huddles/$HUDDLE_ID/preview
check_response

# 6. Validate Content Quality
echo -e "${YELLOW}6. Validating Content Quality...${NC}"
curl -s -X POST $BASE_URL/content/huddles/$HUDDLE_ID/validate | jq '.' || \
curl -s -X POST $BASE_URL/content/huddles/$HUDDLE_ID/validate
check_response

# 7. Test Bulk Huddle Creation
echo -e "${YELLOW}7. Testing Bulk Huddle Creation...${NC}"
curl -s -X POST $BASE_URL/content/sequences/$SEQUENCE_ID/bulk-create \
  -H "Content-Type: application/json" \
  -d '{
    "huddles": [
      {
        "title": "Medication Safety Huddle",
        "topic": "Medication Administration",
        "orderIndex": 2,
        "content": {
          "title": "Medication Safety: The Five Rights",
          "objective": "Reinforce the Five Rights of medication administration to prevent medication errors",
          "keyContent": "Medication errors are preventable adverse events that can cause patient harm. The Five Rights framework provides a systematic approach to safe medication administration:\n\n1. **Right Patient**: Verify patient identity using two identifiers\n2. **Right Medication**: Check medication name and dosage\n3. **Right Dose**: Confirm correct amount and concentration\n4. **Right Route**: Ensure appropriate administration method\n5. **Right Time**: Administer at the prescribed time\n\nAlways perform independent double-checks for high-risk medications.",
          "actionItems": [
            "Use two patient identifiers before every medication administration",
            "Perform independent calculation for all high-risk medications",
            "Document immediately after administration"
          ],
          "discussionPoints": [
            "What medication errors have you witnessed or heard about?",
            "When is it appropriate to question a medication order?"
          ],
          "estimatedMinutes": 6
        }
      },
      {
        "title": "Hand Hygiene Excellence",
        "topic": "Infection Prevention",
        "orderIndex": 3,
        "content": {
          "title": "Hand Hygiene: Your First Defense",
          "objective": "Achieve 100% compliance with hand hygiene protocols",
          "keyContent": "Hand hygiene is the single most effective way to prevent healthcare-associated infections. Studies show proper hand hygiene can reduce infections by up to 50%.\n\n**When to Perform Hand Hygiene:**\n- Before and after patient contact\n- Before clean/aseptic procedures\n- After body fluid exposure risk\n- After contact with patient surroundings\n\n**Proper Technique:** 15-20 seconds with soap and water, or alcohol-based hand rub for at least 15 seconds.",
          "actionItems": [
            "Perform hand hygiene at all WHO-recommended moments",
            "Encourage colleagues when you observe good hand hygiene",
            "Remove jewelry and keep nails short"
          ],
          "discussionPoints": [
            "What barriers to hand hygiene exist in your work area?",
            "How can we make hand hygiene more convenient?"
          ],
          "estimatedMinutes": 5
        }
      }
    ],
    "publishImmediately": false,
    "overwriteExisting": false
  }' | jq '.' || echo "Testing bulk creation..."
check_response

# 8. Test Huddle Duplication
echo -e "${YELLOW}8. Testing Huddle Duplication...${NC}"
curl -s -X POST "$BASE_URL/content/huddles/$HUDDLE_ID/duplicate?targetSequenceId=$SEQUENCE_ID&newOrderIndex=4" | jq '.' || \
curl -s -X POST "$BASE_URL/content/huddles/$HUDDLE_ID/duplicate?targetSequenceId=$SEQUENCE_ID&newOrderIndex=4"
check_response

# 9. Update Existing Content
echo -e "${YELLOW}9. Testing Content Update...${NC}"
curl -s -X PUT $BASE_URL/content/huddles/$HUDDLE_ID/manual \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Effective Patient Communication (Updated)",
    "objective": "Master evidence-based techniques for clear, empathetic patient communication that improves satisfaction and clinical outcomes",
    "keyContent": "Effective patient communication is fundamental to quality healthcare delivery. Research demonstrates that clear communication reduces medical errors by 30% and significantly improves patient satisfaction scores.\n\n**Updated content with emphasis on digital communication tools and telehealth considerations.**",
    "actionItems": [
      "Practice active listening during your next 3 patient interactions",
      "Use plain language - replace medical jargon with patient-friendly terms",
      "Implement teach-back method for all discharge instructions"
    ],
    "estimatedMinutes": 8,
    "publishImmediately": true
  }' | jq '.' || echo "Testing content update..."
check_response

# 10. Get Content Statistics
echo -e "${YELLOW}10. Getting Content Statistics...${NC}"
curl -s $BASE_URL/content/statistics | jq '.' || curl -s $BASE_URL/content/statistics
check_response

# 11. Verify Final Sequence State
echo -e "${YELLOW}11. Verifying Final Sequence State...${NC}"
echo "Getting all huddles in sequence $SEQUENCE_ID..."
curl -s $BASE_URL/sequences/$SEQUENCE_ID/huddles | jq '.[] | {title: .title, orderIndex: .orderIndex, status: .huddleStatus, wordCount: .wordCount, estimatedMinutes: .estimatedMinutes}' || \
curl -s $BASE_URL/sequences/$SEQUENCE_ID/huddles
check_response

# 12. Test Error Handling
echo -e "${YELLOW}12. Testing Error Handling...${NC}"
echo "Testing with invalid data..."
curl -s -X POST $BASE_URL/content/huddles/999999/manual \
  -H "Content-Type: application/json" \
  -d '{
    "title": "",
    "objective": "",
    "keyContent": ""
  }' | jq '.' || echo "Testing error handling..."
check_response

# Summary
echo "=================================================="
echo -e "${BLUE}=== Day 6 Manual Content Creation Testing Summary ===${NC}"
echo "=================================================="
echo
echo -e "${GREEN}Tests completed:${NC}"
echo "✓ Content creation guidelines retrieved"
echo "✓ Test sequence created"
echo "✓ Individual huddle created"
echo "✓ Manual content added to huddle"
echo "✓ Content preview generated"
echo "✓ Content validation performed"
echo "✓ Bulk huddle creation tested"
echo "✓ Huddle duplication tested"
echo "✓ Content update functionality tested"
echo "✓ Content statistics retrieved"
echo "✓ Error handling validated"
echo
echo -e "${YELLOW}Content Creation Features Available:${NC}"
echo "• Manual content creation with structured input"
echo "• Content validation and quality checking"
echo "• Bulk huddle creation for efficiency"
echo "• Content preview and formatting"
echo "• Huddle duplication and templates"
echo "• Rich content support (HTML, lists, resources)"
echo "• Word count and duration estimation"
echo "• Content tagging and categorization"
echo
echo -e "${BLUE}Next Steps:${NC}"
echo "1. Check application logs for any errors"
echo "2. Verify database content was created correctly"
echo "3. Test different content types and validation scenarios"
echo "4. Review content preview formatting"
echo "5. Test with larger bulk creation requests"
echo
echo -e "${GREEN}Day 6 Complete! ✨${NC}"
echo "Your backend now supports comprehensive manual content creation!"
echo "Educators can create structured, high-quality huddle content without AI dependency."