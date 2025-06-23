package com.hqc.hophuddles.controller;

import com.hqc.hophuddles.entity.*;
import com.hqc.hophuddles.enums.*;
import com.hqc.hophuddles.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private AgencyRepository agencyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAssignmentRepository userAssignmentRepository;

    @Autowired
    private SequenceTargetRepository sequenceTargetRepository;

    @Autowired
    private HuddleSequenceRepository sequenceRepository;

    @Autowired
    private HuddleRepository huddleRepository;

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Autowired
    private SequenceProgressRepository sequenceProgressRepository;

    @Autowired
    private EngagementEventRepository engagementEventRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @PostMapping("/create-sample-data")
    public ResponseEntity<Map<String, Object>> createSampleData() {
        try {
            // Create sample agency
            Agency agency = new Agency("ABC Home Health", "123456", AgencyType.HOME_HEALTH);
            agency.setSubscriptionPlan(SubscriptionPlan.PREMIUM);
            agency.setContactEmail("admin@abchomehealth.com");
            agency.setContactPhone("555-0123");
            agency.setAddress("123 Healthcare Drive, Medical City, HC 12345");
            agency = agencyRepository.save(agency);

            // Create educator user
            User educator = new User("auth0|educator123", "educator@abchomehealth.com", "Jane Smith");
            educator.setPhone("555-1111");
            educator = userRepository.save(educator);

            // Create learner user
            User learner = new User("auth0|learner123", "john.doe@abchomehealth.com", "John Doe");
            learner.setPhone("555-2222");
            learner = userRepository.save(learner);

            // Create educator assignment
            UserAssignment educatorAssignment = new UserAssignment(educator, agency, UserRole.EDUCATOR);
            educatorAssignment.setDiscipline(Discipline.RN);
            educatorAssignment.setIsPrimary(true);
            educatorAssignment = userAssignmentRepository.save(educatorAssignment);

            // Create learner assignment
            UserAssignment learnerAssignment = new UserAssignment(learner, agency, UserRole.FIELD_CLINICIAN);
            learnerAssignment.setDiscipline(Discipline.RN);
            learnerAssignment.setIsPrimary(true);
            learnerAssignment = userAssignmentRepository.save(learnerAssignment);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Enhanced sample data created successfully");
            response.put("agencyId", agency.getAgencyId());
            response.put("educatorId", educator.getUserId());
            response.put("learnerId", learner.getUserId());
            response.put("assignments", List.of(
                    Map.of("id", educatorAssignment.getAssignmentId(), "role", "EDUCATOR"),
                    Map.of("id", learnerAssignment.getAssignmentId(), "role", "FIELD_CLINICIAN")
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/verify-data")
    public ResponseEntity<Map<String, Object>> verifyData() {
        Map<String, Object> response = new HashMap<>();

        // Count entities
        long agencyCount = agencyRepository.count();
        long userCount = userRepository.count();
        long assignmentCount = userAssignmentRepository.count();

        response.put("agencyCount", agencyCount);
        response.put("userCount", userCount);
        response.put("assignmentCount", assignmentCount);
        response.put("tablesCreated", agencyCount >= 0 && userCount >= 0 && assignmentCount >= 0);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/agency/{agencyId}/users")
    public ResponseEntity<Map<String, Object>> getAgencyUsers(@PathVariable Long agencyId) {
        try {
            List<User> users = userRepository.findActiveUsersByAgency(agencyId);
            List<Object[]> roleStats = userAssignmentRepository.countUsersByRoleInAgency(agencyId);

            Map<String, Object> response = new HashMap<>();
            response.put("agencyId", agencyId);
            response.put("userCount", users.size());
            response.put("users", users.stream().map(u -> Map.of(
                    "userId", u.getUserId(),
                    "name", u.getName(),
                    "email", u.getEmail()
            )).toList());
            response.put("roleStats", roleStats);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/assignments")
    public ResponseEntity<Map<String, Object>> getUserAssignments(@PathVariable Long userId) {
        try {
            List<UserAssignment> assignments = userAssignmentRepository
                    .findByUserUserIdAndIsActiveTrueOrderByIsPrimaryDescAssignedAtDesc(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("assignmentCount", assignments.size());
            response.put("assignments", assignments.stream().map(a -> Map.of(
                    "assignmentId", a.getAssignmentId(),
                    "agencyName", a.getAgency().getName(),
                    "role", a.getRole().getDisplayName(),
                    "discipline", a.getDiscipline() != null ? a.getDiscipline().getDisplayName() : "N/A",
                    "isPrimary", a.getIsPrimary(),
                    "accessScope", a.getAccessScope()
            )).toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Add these methods to your existing TestController.java

    @PostMapping("/create-huddle-sample-data")
    public ResponseEntity<Map<String, Object>> createHuddleSampleData() {
        try {
            // Create agency and user first
            Agency agency = new Agency("HOP Test Agency", "111111", AgencyType.HOME_HEALTH);
            agency = agencyRepository.save(agency);

            User educator = new User("auth0|educator789", "educator@hoptest.com", "Dr. Sarah Johnson");
            educator = userRepository.save(educator);

            // Create educator assignment
            UserAssignment assignment = new UserAssignment(educator, agency, UserRole.EDUCATOR);
            assignment.setDiscipline(Discipline.RN);
            assignment.setIsPrimary(true);
            userAssignmentRepository.save(assignment);

            // Create sequence
            HuddleSequence sequence = new HuddleSequence(agency, "Fall Prevention Training", educator);
            sequence.setDescription("Comprehensive fall prevention protocols for home health");
            sequence.setTopic("Patient safety and fall prevention strategies");
            sequence.setEstimatedDurationMinutes(45);
            sequence.setGenerationPrompt("Create a comprehensive fall prevention training for RN field clinicians");
            sequence = sequenceRepository.save(sequence);

            // Create sample huddles
            Huddle huddle1 = new Huddle(sequence, "Introduction to Fall Prevention", 1);
            huddle1.setHuddleType(HuddleType.INTRO);
            huddle1.setDurationMinutes(10);
            huddle1.setContentJson("{\"sections\":[{\"title\":\"Introduction\",\"content\":\"Welcome to fall prevention training...\"}]}");
            huddle1.setVoiceScript("Welcome to our comprehensive fall prevention training. Today we'll cover essential strategies...");
            huddleRepository.save(huddle1);

            Huddle huddle2 = new Huddle(sequence, "Risk Assessment Techniques", 2);
            huddle2.setHuddleType(HuddleType.STANDARD);
            huddle2.setDurationMinutes(20);
            huddle2.setContentJson("{\"sections\":[{\"title\":\"Risk Assessment\",\"content\":\"Learn to identify fall risks...\"}]}");
            huddle2.setVoiceScript("Risk assessment is crucial for preventing falls. Let's examine the key factors...");
            huddleRepository.save(huddle2);

            Huddle huddle3 = new Huddle(sequence, "Prevention Strategies", 3);
            huddle3.setHuddleType(HuddleType.STANDARD);
            huddle3.setDurationMinutes(15);
            huddle3.setContentJson("{\"sections\":[{\"title\":\"Prevention\",\"content\":\"Implement effective prevention strategies...\"}]}");
            huddle3.setVoiceScript("Now let's discuss proven prevention strategies that you can implement immediately...");
            huddleRepository.save(huddle3);

            // Update sequence total huddles
            sequence.setTotalHuddles(3);
            sequenceRepository.save(sequence);

            // Add targets
            SequenceTarget target1 = new SequenceTarget(sequence, TargetType.DISCIPLINE, "RN");
            sequenceTargetRepository.save(target1);

            SequenceTarget target2 = new SequenceTarget(sequence, TargetType.ROLE, "FIELD_CLINICIAN");
            sequenceTargetRepository.save(target2);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Huddle sample data created successfully");
            response.put("agencyId", agency.getAgencyId());
            response.put("educatorId", educator.getUserId());
            response.put("sequenceId", sequence.getSequenceId());
            response.put("huddleCount", 3);
            response.put("targetCount", 2);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/huddle-stats")
    public ResponseEntity<Map<String, Object>> getHuddleStats() {
        Map<String, Object> response = new HashMap<>();

        long sequenceCount = sequenceRepository.count();
        long huddleCount = huddleRepository.count();
        long targetCount = sequenceTargetRepository.count();

        response.put("sequenceCount", sequenceCount);
        response.put("huddleCount", huddleCount);
        response.put("targetCount", targetCount);
        response.put("tablesCreated", sequenceCount >= 0 && huddleCount >= 0 && targetCount >= 0);

        return ResponseEntity.ok(response);
    }

    // Add these methods to your existing TestController.java

    @PostMapping("/create-progress-sample-data")
    public ResponseEntity<Map<String, Object>> createProgressSampleData() {
        try {
            // Use existing sample data from Day 4
            // Find existing sequence and users
            Optional<HuddleSequence> sequenceOpt = sequenceRepository.findAll().stream().findFirst();
            if (sequenceOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No sequences found. Run create-huddle-sample-data first."));
            }

            HuddleSequence sequence = sequenceOpt.get();
            List<Huddle> huddles = huddleRepository.findBySequenceSequenceIdAndIsActiveTrueOrderByOrderIndexAsc(sequence.getSequenceId());

            // Find a learner user
            Optional<User> learnerOpt = userRepository.findAll().stream()
                    .filter(u -> u.getEmail().contains("learner") || u.getEmail().contains("john"))
                    .findFirst();

            if (learnerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No learner user found. Run create-sample-data first."));
            }

            User learner = learnerOpt.get();

            // Create progress for each huddle
            for (int i = 0; i < huddles.size(); i++) {
                Huddle huddle = huddles.get(i);

                UserProgress progress = UserProgress.builder()
                        .user(learner)
                        .huddle(huddle)
                        .sequence(sequence)
                        .progressStatus(i == 0 ? ProgressStatus.COMPLETED :
                                i == 1 ? ProgressStatus.IN_PROGRESS : ProgressStatus.NOT_STARTED)
                        .completionPercentage(i == 0 ? new BigDecimal("100.00") :
                                i == 1 ? new BigDecimal("60.00") : BigDecimal.ZERO)
                        .timeSpentMinutes(i == 0 ? new BigDecimal("12.50") :
                                i == 1 ? new BigDecimal("8.00") : BigDecimal.ZERO)
                        .build();

                if (i == 0) {
                    progress.setStartedAt(LocalDateTime.now().minusDays(2));
                    progress.setCompletedAt(LocalDateTime.now().minusDays(1));
                    progress.setAssessmentScore(new BigDecimal("85.00"));
                    progress.setAssessmentAttempts(1);
                } else if (i == 1) {
                    progress.setStartedAt(LocalDateTime.now().minusHours(3));
                }

                progress.setLastAccessed(LocalDateTime.now().minusMinutes(30));
                userProgressRepository.save(progress);
            }

            // Create sequence progress
            SequenceProgress sequenceProgress = SequenceProgress.builder()
                    .user(learner)
                    .sequence(sequence)
                    .agency(sequence.getAgency())
                    .totalHuddles(huddles.size())
                    .completedHuddles(1)
                    .completionPercentage(new BigDecimal("33.33"))
                    .totalTimeSpentMinutes(new BigDecimal("20.50"))
                    .averageScore(new BigDecimal("85.00"))
                    .sequenceStatus(ProgressStatus.IN_PROGRESS)
                    .startedAt(LocalDateTime.now().minusDays(2))
                    .lastAccessed(LocalDateTime.now().minusMinutes(30))
                    .build();

            sequenceProgressRepository.save(sequenceProgress);

            // Create some engagement events
            for (int i = 0; i < 5; i++) {
                EngagementEvent event = EngagementEvent.builder()
                        .user(learner)
                        .huddle(huddles.get(0))
                        .sequence(sequence)
                        .agency(sequence.getAgency())
                        .eventType(i % 2 == 0 ? EventType.VIEW : EventType.PLAY_AUDIO)
                        .sessionId("session_" + System.currentTimeMillis())
                        .build();

                engagementEventRepository.save(event);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Progress sample data created successfully");
            response.put("learnerId", learner.getUserId());
            response.put("sequenceId", sequence.getSequenceId());
            response.put("progressCount", huddles.size());
            response.put("eventCount", 5);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/progress-stats")
    public ResponseEntity<Map<String, Object>> getProgressStats() {
        Map<String, Object> response = new HashMap<>();

        long userProgressCount = userProgressRepository.count();
        long sequenceProgressCount = sequenceProgressRepository.count();
        long engagementEventCount = engagementEventRepository.count();
        long assessmentCount = assessmentRepository.count();

        response.put("userProgressCount", userProgressCount);
        response.put("sequenceProgressCount", sequenceProgressCount);
        response.put("engagementEventCount", engagementEventCount);
        response.put("assessmentCount", assessmentCount);
        response.put("tablesCreated", userProgressCount >= 0 && sequenceProgressCount >= 0 && engagementEventCount >= 0);

        return ResponseEntity.ok(response);
    }
}