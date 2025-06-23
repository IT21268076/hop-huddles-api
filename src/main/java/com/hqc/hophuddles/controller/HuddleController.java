package com.hqc.hophuddles.controller;

import com.hqc.hophuddles.dto.request.HuddleCreateRequest;
import com.hqc.hophuddles.dto.response.HuddleResponse;
import com.hqc.hophuddles.enums.HuddleType;
import com.hqc.hophuddles.service.HuddleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/huddles")
@CrossOrigin(origins = "*")
public class HuddleController {

    @Autowired
    private HuddleService huddleService;

    @PostMapping
    public ResponseEntity<HuddleResponse> createHuddle(@Valid @RequestBody HuddleCreateRequest request) {
        HuddleResponse response = huddleService.createHuddle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{huddleId}")
    public ResponseEntity<HuddleResponse> getHuddle(@PathVariable Long huddleId) {
        HuddleResponse response = huddleService.getHuddleById(huddleId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sequence/{sequenceId}")
    public ResponseEntity<List<HuddleResponse>> getHuddlesBySequence(@PathVariable Long sequenceId) {
        List<HuddleResponse> huddles = huddleService.getHuddlesBySequence(sequenceId);
        return ResponseEntity.ok(huddles);
    }

    @GetMapping("/sequence/{sequenceId}/type/{huddleType}")
    public ResponseEntity<List<HuddleResponse>> getHuddlesBySequenceAndType(
            @PathVariable Long sequenceId,
            @PathVariable HuddleType huddleType) {
        List<HuddleResponse> huddles = huddleService.getHuddlesBySequenceAndType(sequenceId, huddleType);
        return ResponseEntity.ok(huddles);
    }

    @GetMapping("/sequence/{sequenceId}/order/{orderIndex}")
    public ResponseEntity<HuddleResponse> getHuddleByOrder(
            @PathVariable Long sequenceId,
            @PathVariable Integer orderIndex) {
        HuddleResponse response = huddleService.getHuddleBySequenceAndOrder(sequenceId, orderIndex);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{huddleId}")
    public ResponseEntity<HuddleResponse> updateHuddle(
            @PathVariable Long huddleId,
            @Valid @RequestBody HuddleCreateRequest request) {
        HuddleResponse response = huddleService.updateHuddle(huddleId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{huddleId}/content")
    public ResponseEntity<HuddleResponse> updateHuddleContent(
            @PathVariable Long huddleId,
            @RequestBody Map<String, String> contentUpdate) {
        String contentJson = contentUpdate.get("contentJson");
        String voiceScript = contentUpdate.get("voiceScript");
        HuddleResponse response = huddleService.updateHuddleContent(huddleId, contentJson, voiceScript);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{huddleId}/files")
    public ResponseEntity<HuddleResponse> updateHuddleFiles(
            @PathVariable Long huddleId,
            @RequestBody Map<String, String> fileUpdate) {
        String pdfUrl = fileUpdate.get("pdfUrl");
        String audioUrl = fileUpdate.get("audioUrl");
        HuddleResponse response = huddleService.updateHuddleFiles(huddleId, pdfUrl, audioUrl);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{huddleId}")
    public ResponseEntity<Void> deleteHuddle(@PathVariable Long huddleId) {
        huddleService.deleteHuddle(huddleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sequence/{sequenceId}/stats")
    public ResponseEntity<Map<String, Object>> getSequenceStats(@PathVariable Long sequenceId) {
        List<HuddleResponse> huddles = huddleService.getHuddlesBySequence(sequenceId);
        long completeCount = huddleService.getCompleteHuddleCount(sequenceId);

        Map<String, Object> stats = Map.of(
                "totalHuddles", huddles.size(),
                "completeHuddles", completeCount,
                "completionPercentage", huddles.isEmpty() ? 0 : (completeCount * 100.0 / huddles.size())
        );

        return ResponseEntity.ok(stats);
    }
}