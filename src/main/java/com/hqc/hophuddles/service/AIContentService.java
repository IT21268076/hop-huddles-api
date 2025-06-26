package com.hqc.hophuddles.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hqc.hophuddles.dto.request.AIGenerationRequest;

import com.hqc.hophuddles.dto.response.AIGenerationResponse;
import com.hqc.hophuddles.dto.response.GeneratedHuddle;
import com.hqc.hophuddles.entity.*;
import com.hqc.hophuddles.enums.GenerationStatus;
import com.hqc.hophuddles.enums.HuddleType;
import com.hqc.hophuddles.exception.ResourceNotFoundException;
import com.hqc.hophuddles.repository.AIGenerationJobRepository;
import com.hqc.hophuddles.repository.HuddleRepository;
import com.hqc.hophuddles.repository.HuddleSequenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AIContentService {

    private final AIGenerationJobRepository generationJobRepository;
    private final HuddleSequenceRepository sequenceRepository;
    private final HuddleRepository huddleRepository;
    private final PDFGenerationService pdfGenerationService;
    private final NotificationService notificationService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.service.base-url:http://localhost:8001}")
    private String aiServiceBaseUrl;

    @Value("${ai.service.api-key}")
    private String aiServiceApiKey;

    @Value("${ai.service.timeout:300000}")
    private long aiServiceTimeout;

    /**
     * Initiate AI content generation for a huddle sequence
     */
    @Async("aiTaskExecutor")
    public CompletableFuture<AIGenerationJob> generateHuddleContent(AIGenerationRequest request) {
        log.info("Starting AI content generation for sequence {}", request.getSequenceId());

        try {
            // Validate sequence exists
            HuddleSequence sequence = sequenceRepository.findById(request.getSequenceId())
                    .orElseThrow(() -> new ResourceNotFoundException("HuddleSequence", request.getSequenceId()));

            // Create generation job record
            AIGenerationJob job = createGenerationJob(sequence, request);

            // Call AI service
            String externalJobId = callAIService(request);

            // Update job with external ID
            job.setExternalJobId(externalJobId);
            job.markStarted();
            generationJobRepository.save(job);

            log.info("AI generation job {} started for sequence {}", job.getGenerationJobId(), request.getSequenceId());

            return CompletableFuture.completedFuture(job);

        } catch (Exception e) {
            log.error("Failed to start AI generation for sequence {}: {}", request.getSequenceId(), e.getMessage(), e);
            throw new RuntimeException("Failed to start AI generation", e);
        }
    }

    /**
     * Create a generation job record
     */
    private AIGenerationJob createGenerationJob(HuddleSequence sequence, AIGenerationRequest request) {
        try {
            String requestJson = objectMapper.writeValueAsString(request);

            AIGenerationJob job = AIGenerationJob.builder()
                    .sequence(sequence)
                    .jobStatus(GenerationStatus.PENDING)
                    .requestPayload(requestJson)
                    .modelVersion(request.getModelVersion())
                    .build();

            return generationJobRepository.save(job);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create generation job", e);
        }
    }

    /**
     * Call external AI service
     */
    private String callAIService(AIGenerationRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiServiceApiKey);

            HttpEntity<AIGenerationRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<AIGenerationResponse> response = restTemplate.postForEntity(
                    aiServiceBaseUrl + "/api/generate-content",
                    entity,
                    AIGenerationResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody().getJobId();
            } else {
                throw new RuntimeException("AI service returned error: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Failed to call AI service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call AI service", e);
        }
    }

    /**
     * Handle AI generation completion webhook
     */
    @Transactional
    public void handleGenerationComplete(AIGenerationResponse response) {
        log.info("Received AI generation completion for job {}", response.getJobId());

        try {
            AIGenerationJob job = generationJobRepository.findByExternalJobId(response.getJobId())
                    .orElseThrow(() -> new ResourceNotFoundException("AI Generation Job not found: " + response.getJobId()));

            if (response.getStatus() == GenerationStatus.COMPLETED) {
                processSuccessfulGeneration(job, response);
            } else {
                processFailedGeneration(job, response);
            }

        } catch (Exception e) {
            log.error("Failed to handle generation completion for job {}: {}", response.getJobId(), e.getMessage(), e);
        }
    }

    /**
     * Process successful AI generation
     */
    @Async("aiTaskExecutor")
    public CompletableFuture<Void> processSuccessfulGeneration(AIGenerationJob job, AIGenerationResponse response) {
        try {
            log.info("Processing successful generation for job {}", job.getGenerationJobId());

            // Store response data
            String responseJson = objectMapper.writeValueAsString(response);
            String contentJson = objectMapper.writeValueAsString(response.getGeneratedHuddles());
            String filesJson = objectMapper.writeValueAsString(response.getGeneratedFiles());

            job.setResponsePayload(responseJson);
            job.markCompleted(contentJson, filesJson);

            if (response.getMetadata() != null) {
                job.setTokensUsed(response.getMetadata().getTokensUsed());
                job.setModelVersion(response.getMetadata().getModelUsed());
                job.setQualityScore(Float.valueOf(response.getMetadata().getQualityScore()));
            }

            generationJobRepository.save(job);

            // Create individual huddles from generated content
            createHuddlesFromGeneration(job.getSequence(), response.getGeneratedHuddles());

            // Generate PDF and audio files if needed
            generateFiles(job, response.getGeneratedHuddles());

            // Send completion notification
            notificationService.sendGenerationCompleteNotification(job);

            log.info("Successfully processed generation for job {}", job.getGenerationJobId());

        } catch (Exception e) {
            log.error("Failed to process successful generation for job {}: {}", job.getGenerationJobId(), e.getMessage(), e);
            processFailedGeneration(job, null);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Process failed AI generation
     */
    private void processFailedGeneration(AIGenerationJob job, AIGenerationResponse response) {
        try {
            String errorMessage = response != null ? response.getErrorMessage() : "Unknown error";
            String errorCode = response != null ? response.getErrorCode() : "UNKNOWN";

            job.markFailed(errorMessage, errorCode);
            generationJobRepository.save(job);

            // Send failure notification
            notificationService.sendGenerationFailureNotification(job);

            log.error("AI generation failed for job {}: {}", job.getGenerationJobId(), errorMessage);

        } catch (Exception e) {
            log.error("Failed to process generation failure for job {}: {}", job.getGenerationJobId(), e.getMessage(), e);
        }
    }

    /**
     * Create huddles from AI-generated content
     */
    private void createHuddlesFromGeneration(HuddleSequence sequence, List<GeneratedHuddle> generatedHuddles) {
        try {
            for (GeneratedHuddle generated : generatedHuddles) {
                Huddle huddle = new Huddle();
                huddle.setSequence(sequence);
                huddle.setTitle(generated.getTitle());
                huddle.setOrderIndex(generated.getOrderIndex());
                huddle.setDurationMinutes(generated.getEstimatedDuration());

                // Map huddle type
                huddle.setHuddleType(mapHuddleType(generated.getHuddleType()));

                // Create structured content JSON
                String contentJson = createContentJson(generated);
                huddle.setContentJson(contentJson);
                huddle.setVoiceScript(generated.getVoiceScript());

                // Store generation metadata
                String metadataJson = objectMapper.writeValueAsString(generated.getMetadata());
                huddle.setGenerationMetadata(metadataJson);

                huddleRepository.save(huddle);

                // Create assessments if present
                if (generated.getAssessmentQuestions() != null && !generated.getAssessmentQuestions().isEmpty()) {
                    createAssessment(huddle, generated.getAssessmentQuestions());
                }
            }

            // Update sequence total huddles
            sequence.setTotalHuddles(generatedHuddles.size());
            sequenceRepository.save(sequence);

        } catch (Exception e) {
            log.error("Failed to create huddles from generation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create huddles", e);
        }
    }

    /**
     * Map string huddle type to enum
     */
    private HuddleType mapHuddleType(String typeString) {
        try {
            return HuddleType.valueOf(typeString.toUpperCase());
        } catch (Exception e) {
            return HuddleType.STANDARD;
        }
    }

    /**
     * Create structured content JSON from generated huddle
     */
    private String createContentJson(GeneratedHuddle generated) throws Exception {
        var content = new java.util.HashMap<String, Object>();
        content.put("introduction", generated.getIntroduction());
        content.put("mainContent", generated.getMainContent());
        content.put("summary", generated.getSummary());
        content.put("keyTakeaways", generated.getKeyTakeaways());

        if (generated.getInteractiveElements() != null) {
            content.put("interactiveElements", generated.getInteractiveElements());
        }

        return objectMapper.writeValueAsString(content);
    }

    /**
     * Create assessment from generated questions
     */
    private void createAssessment(Huddle huddle, List<com.hqc.hophuddles.dto.request.AssessmentQuestion> questions) {
        try {
            Assessment assessment = Assessment.builder()
                    .huddle(huddle)
                    .questionsJson(objectMapper.writeValueAsString(questions))
                    .passingScore(new java.math.BigDecimal("70.00"))
                    .maxAttempts(3)
                    .build();

            // Save assessment (you'd need AssessmentRepository)
            // assessmentRepository.save(assessment);

        } catch (Exception e) {
            log.error("Failed to create assessment for huddle {}: {}", huddle.getHuddleId(), e.getMessage(), e);
        }
    }

    /**
     * Generate PDF and audio files
     */
    @Async("fileGenerationExecutor")
    public CompletableFuture<Void> generateFiles(AIGenerationJob job, List<GeneratedHuddle> huddles) {
        try {
            for (GeneratedHuddle huddle : huddles) {
                // Generate PDF
                String pdfContent = createPDFContent(huddle);
                String pdfPath = pdfGenerationService.generateSimplePdf(
                        pdfContent,
                        huddle.getTitle(),
                        "ai_generated"
                );

                // TODO: Generate audio file using TTS service
                // String audioPath = ttsService.generateAudio(huddle.getVoiceScript(), huddle.getTitle());

                log.info("Generated files for huddle: {}", huddle.getTitle());
            }

        } catch (Exception e) {
            log.error("Failed to generate files for job {}: {}", job.getGenerationJobId(), e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Create PDF content from generated huddle
     */
    private String createPDFContent(GeneratedHuddle huddle) {
        StringBuilder content = new StringBuilder();
        content.append("# ").append(huddle.getTitle()).append("\n\n");

        if (huddle.getIntroduction() != null) {
            content.append("## Introduction\n").append(huddle.getIntroduction()).append("\n\n");
        }

        if (huddle.getMainContent() != null) {
            content.append("## Main Content\n").append(huddle.getMainContent()).append("\n\n");
        }

        if (huddle.getSummary() != null) {
            content.append("## Summary\n").append(huddle.getSummary()).append("\n\n");
        }

        if (huddle.getKeyTakeaways() != null) {
            content.append("## Key Takeaways\n").append(huddle.getKeyTakeaways()).append("\n\n");
        }

        return content.toString();
    }

    /**
     * Get generation job status
     */
    public AIGenerationJob getJobStatus(Long jobId) {
        return generationJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("AI Generation Job", jobId));
    }

    /**
     * Get generation jobs for a sequence
     */
    public List<AIGenerationJob> getSequenceGenerationJobs(Long sequenceId) {
        return generationJobRepository.findBySequenceSequenceIdOrderByCreatedAtDesc(sequenceId);
    }

    /**
     * Retry failed generation
     */
    public void retryGeneration(Long jobId) {
        AIGenerationJob job = getJobStatus(jobId);

        if (!job.canRetry()) {
            throw new IllegalStateException("Job cannot be retried");
        }

        try {
            // Parse original request
            AIGenerationRequest request = objectMapper.readValue(job.getRequestPayload(), AIGenerationRequest.class);

            // Reset job status
            job.setJobStatus(GenerationStatus.PENDING);
            job.setErrorMessage(null);
            job.setErrorCode(null);
            generationJobRepository.save(job);

            // Restart generation
            generateHuddleContent(request);

        } catch (Exception e) {
            log.error("Failed to retry generation for job {}: {}", jobId, e.getMessage(), e);
            throw new RuntimeException("Failed to retry generation", e);
        }
    }

    /**
     * Monitor stale jobs (runs every 5 minutes)
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void monitorStaleJobs() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
        List<AIGenerationJob> staleJobs = generationJobRepository.findStaleJobs(cutoff);

        for (AIGenerationJob job : staleJobs) {
            log.warn("Found stale AI generation job: {}", job.getGenerationJobId());

            // Check status with AI service
            checkJobStatusWithAIService(job);
        }
    }

    /**
     * Check job status with AI service
     */
    private void checkJobStatusWithAIService(AIGenerationJob job) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(aiServiceApiKey);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<AIGenerationResponse> response = restTemplate.exchange(
                    aiServiceBaseUrl + "/api/job-status/" + job.getExternalJobId(),
                    HttpMethod.GET,
                    entity,
                    AIGenerationResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                AIGenerationResponse status = response.getBody();

                if (status.getStatus().isTerminal()) {
                    handleGenerationComplete(status);
                }
            }

        } catch (Exception e) {
            log.error("Failed to check job status for {}: {}", job.getExternalJobId(), e.getMessage(), e);
        }
    }

    /**
     * Auto-retry failed jobs (runs every hour)
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void autoRetryFailedJobs() {
        List<AIGenerationJob> retryableJobs = generationJobRepository.findRetryableJobs();

        for (AIGenerationJob job : retryableJobs) {
            try {
                retryGeneration(job.getGenerationJobId());
                log.info("Auto-retried failed generation job: {}", job.getGenerationJobId());
            } catch (Exception e) {
                log.error("Failed to auto-retry job {}: {}", job.getGenerationJobId(), e.getMessage(), e);
            }
        }
    }
}