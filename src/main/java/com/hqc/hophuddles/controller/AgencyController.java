package com.hqc.hophuddles.controller;

import com.hqc.hophuddles.dto.request.AgencyCreateRequest;
import com.hqc.hophuddles.dto.response.AgencyResponse;
import com.hqc.hophuddles.enums.AgencyType;
import com.hqc.hophuddles.service.AgencyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agencies")
@CrossOrigin(origins = "*")
public class AgencyController {

    @Autowired
    private AgencyService agencyService;

    @PostMapping
    public ResponseEntity<AgencyResponse> createAgency(@Valid @RequestBody AgencyCreateRequest request) {
        AgencyResponse response = agencyService.createAgency(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{agencyId}")
    public ResponseEntity<AgencyResponse> getAgency(@PathVariable Long agencyId) {
        AgencyResponse response = agencyService.getAgencyById(agencyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AgencyResponse>> getAllAgencies() {
        List<AgencyResponse> agencies = agencyService.getAllActiveAgencies();
        return ResponseEntity.ok(agencies);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AgencyResponse>> searchAgencies(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) AgencyType agencyType,
            Pageable pageable) {
        Page<AgencyResponse> agencies = agencyService.searchAgencies(name, agencyType, pageable);
        return ResponseEntity.ok(agencies);
    }

    @GetMapping("/type/{agencyType}")
    public ResponseEntity<List<AgencyResponse>> getAgenciesByType(@PathVariable AgencyType agencyType) {
        List<AgencyResponse> agencies = agencyService.getAgenciesByType(agencyType);
        return ResponseEntity.ok(agencies);
    }

    @PutMapping("/{agencyId}")
    public ResponseEntity<AgencyResponse> updateAgency(
            @PathVariable Long agencyId,
            @Valid @RequestBody AgencyCreateRequest request) {
        AgencyResponse response = agencyService.updateAgency(agencyId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{agencyId}")
    public ResponseEntity<Void> deleteAgency(@PathVariable Long agencyId) {
        agencyService.deleteAgency(agencyId);
        return ResponseEntity.noContent().build();
    }
}