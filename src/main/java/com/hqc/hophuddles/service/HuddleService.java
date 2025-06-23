package com.hqc.hophuddles.service;

import com.hqc.hophuddles.dto.request.HuddleCreateRequest;
import com.hqc.hophuddles.dto.response.HuddleResponse;
import com.hqc.hophuddles.entity.Huddle;
import com.hqc.hophuddles.entity.HuddleSequence;
import com.hqc.hophuddles.enums.HuddleType;
import com.hqc.hophuddles.exception.ResourceNotFoundException;
import com.hqc.hophuddles.exception.ValidationException;
import com.hqc.hophuddles.repository.HuddleRepository;
import com.hqc.hophuddles.repository.HuddleSequenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HuddleService {

    @Autowired
    private HuddleRepository huddleRepository;

    @Autowired
    private HuddleSequenceRepository sequenceRepository;

    public HuddleResponse createHuddle(HuddleCreateRequest request) {
        // Validate sequence exists and can be edited
        HuddleSequence sequence = sequenceRepository.findById(request.getSequenceId())
                .filter(s -> s.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("HuddleSequence", request.getSequenceId()));

        if (!sequence.canEdit()) {
            throw new ValidationException("Cannot add huddles to sequence in " + sequence.getSequenceStatus() + " status");
        }

        // Determine order index
        Integer orderIndex = request.getOrderIndex();
        if (orderIndex == null) {
            orderIndex = huddleRepository.getNextOrderIndex(request.getSequenceId());
        } else {
            // Check if order index already exists
            if (huddleRepository.findBySequenceSequenceIdAndOrderIndexAndIsActiveTrue(
                    request.getSequenceId(), orderIndex).isPresent()) {
                throw new ValidationException("Huddle with order index " + orderIndex + " already exists in this sequence");
            }
        }

        // Create huddle
        Huddle huddle = new Huddle();
        huddle.setSequence(sequence);
        huddle.setTitle(request.getTitle());
        huddle.setOrderIndex(orderIndex);
        huddle.setContentJson(request.getContentJson());
        huddle.setVoiceScript(request.getVoiceScript());
        huddle.setDurationMinutes(request.getDurationMinutes());
        huddle.setHuddleType(request.getHuddleType());

        huddle = huddleRepository.save(huddle);

        // Update sequence total huddles
        sequence.setTotalHuddles(sequence.getHuddles().size() + 1);
        sequenceRepository.save(sequence);

        return convertToResponse(huddle);
    }

    @Transactional(readOnly = true)
    public HuddleResponse getHuddleById(Long huddleId) {
        Huddle huddle = huddleRepository.findById(huddleId)
                .filter(h -> h.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("Huddle", huddleId));

        return convertToResponse(huddle);
    }

    @Transactional(readOnly = true)
    public List<HuddleResponse> getHuddlesBySequence(Long sequenceId) {
        return huddleRepository.findBySequenceSequenceIdAndIsActiveTrueOrderByOrderIndexAsc(sequenceId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HuddleResponse> getHuddlesBySequenceAndType(Long sequenceId, HuddleType huddleType) {
        return huddleRepository.findBySequenceSequenceIdAndHuddleTypeAndIsActiveTrueOrderByOrderIndexAsc(sequenceId, huddleType)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HuddleResponse getHuddleBySequenceAndOrder(Long sequenceId, Integer orderIndex) {
        Huddle huddle = huddleRepository.findBySequenceSequenceIdAndOrderIndexAndIsActiveTrue(sequenceId, orderIndex)
                .orElseThrow(() -> new ResourceNotFoundException("Huddle not found with order index " + orderIndex + " in sequence " + sequenceId));

        return convertToResponse(huddle);
    }

    public HuddleResponse updateHuddle(Long huddleId, HuddleCreateRequest request) {
        Huddle huddle = huddleRepository.findById(huddleId)
                .filter(h -> h.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("Huddle", huddleId));

        // Check if sequence can be edited
        if (!huddle.getSequence().canEdit()) {
            throw new ValidationException("Cannot update huddle in sequence with " + huddle.getSequence().getSequenceStatus() + " status");
        }

        // Update fields
        huddle.setTitle(request.getTitle());
        huddle.setContentJson(request.getContentJson());
        huddle.setVoiceScript(request.getVoiceScript());
        huddle.setDurationMinutes(request.getDurationMinutes());
        huddle.setHuddleType(request.getHuddleType());

        // Update order index if provided and different
        if (request.getOrderIndex() != null && !request.getOrderIndex().equals(huddle.getOrderIndex())) {
            // Check if new order index is available
            if (huddleRepository.findBySequenceSequenceIdAndOrderIndexAndIsActiveTrue(
                    huddle.getSequence().getSequenceId(), request.getOrderIndex()).isPresent()) {
                throw new ValidationException("Huddle with order index " + request.getOrderIndex() + " already exists in this sequence");
            }
            huddle.setOrderIndex(request.getOrderIndex());
        }

        huddle = huddleRepository.save(huddle);

        return convertToResponse(huddle);
    }

    public HuddleResponse updateHuddleContent(Long huddleId, String contentJson, String voiceScript) {
        Huddle huddle = huddleRepository.findById(huddleId)
                .filter(h -> h.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("Huddle", huddleId));

        // Check if sequence can be edited
        if (!huddle.getSequence().canEdit()) {
            throw new ValidationException("Cannot update huddle content in sequence with " + huddle.getSequence().getSequenceStatus() + " status");
        }

        huddle.setContentJson(contentJson);
        huddle.setVoiceScript(voiceScript);

        huddle = huddleRepository.save(huddle);

        return convertToResponse(huddle);
    }

    public HuddleResponse updateHuddleFiles(Long huddleId, String pdfUrl, String audioUrl) {
        Huddle huddle = huddleRepository.findById(huddleId)
                .filter(h -> h.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("Huddle", huddleId));

        huddle.setPdfUrl(pdfUrl);
        huddle.setAudioUrl(audioUrl);

        huddle = huddleRepository.save(huddle);

        return convertToResponse(huddle);
    }

    public void deleteHuddle(Long huddleId) {
        Huddle huddle = huddleRepository.findById(huddleId)
                .filter(h -> h.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("Huddle", huddleId));

        // Check if sequence can be edited
        if (!huddle.getSequence().canEdit()) {
            throw new ValidationException("Cannot delete huddle from sequence with " + huddle.getSequence().getSequenceStatus() + " status");
        }

        // Soft delete
        huddle.setIsActive(false);
        huddleRepository.save(huddle);

        // Update sequence total huddles
        HuddleSequence sequence = huddle.getSequence();
        sequence.setTotalHuddles(Math.max(0, sequence.getTotalHuddles() - 1));
        sequenceRepository.save(sequence);
    }

    @Transactional(readOnly = true)
    public long getCompleteHuddleCount(Long sequenceId) {
        return huddleRepository.countCompleteHuddles(sequenceId);
    }

    // Helper method to convert entity to response DTO
    private HuddleResponse convertToResponse(Huddle huddle) {
        HuddleResponse response = new HuddleResponse();
        response.setHuddleId(huddle.getHuddleId());
        response.setSequenceId(huddle.getSequence().getSequenceId());
        response.setSequenceTitle(huddle.getSequence().getTitle());
        response.setTitle(huddle.getTitle());
        response.setOrderIndex(huddle.getOrderIndex());
        response.setContentJson(huddle.getContentJson());
        response.setVoiceScript(huddle.getVoiceScript());
        response.setPdfUrl(huddle.getPdfUrl());
        response.setAudioUrl(huddle.getAudioUrl());
        response.setDurationMinutes(huddle.getDurationMinutes());
        response.setHuddleType(huddle.getHuddleType());
        response.setComplete(huddle.isComplete());
        response.setCreatedAt(huddle.getCreatedAt());

        return response;
    }
}