package com.hqc.hophuddles.service;

import com.hqc.hophuddles.dto.request.AgencyCreateRequest;
import com.hqc.hophuddles.dto.response.AgencyResponse;
import com.hqc.hophuddles.entity.Agency;
import com.hqc.hophuddles.enums.AgencyType;
import com.hqc.hophuddles.exception.ResourceNotFoundException;
import com.hqc.hophuddles.exception.ValidationException;
import com.hqc.hophuddles.repository.AgencyRepository;
import com.hqc.hophuddles.util.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional
public class AgencyService {

    @Autowired
    private AgencyRepository agencyRepository;

    public AgencyResponse createAgency(AgencyCreateRequest request) {
        // Validate unique CCN
        if (request.getCcn() != null && agencyRepository.existsByCcnAndIsActiveTrue(request.getCcn())) {
            throw new ValidationException("Agency with CCN " + request.getCcn() + " already exists");
        }

        // Create and save agency
        Agency agency = new Agency();
        agency.setName(request.getName());
        agency.setCcn(request.getCcn());
        agency.setAgencyType(request.getAgencyType());
        agency.setSubscriptionPlan(request.getSubscriptionPlan());
        agency.setContactEmail(request.getContactEmail());
        agency.setContactPhone(request.getContactPhone());
        agency.setAddress(request.getAddress());

        agency = agencyRepository.save(agency);

        return convertToResponse(agency);
    }

    @Transactional(readOnly = true)
    public AgencyResponse getAgencyById(Long agencyId) {
        Agency agency = agencyRepository.findById(agencyId)
                .filter(a -> a.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));

        return convertToResponse(agency);
    }

    @Transactional(readOnly = true)
    public List<AgencyResponse> getAllActiveAgencies() {
        return agencyRepository.findByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<AgencyResponse> searchAgencies(String name, AgencyType agencyType, Pageable pageable) {
        return agencyRepository.findAgenciesWithFilters(name, agencyType, pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public List<AgencyResponse> getAgenciesByType(AgencyType agencyType) {
        return agencyRepository.findByAgencyTypeAndIsActiveTrueOrderByNameAsc(agencyType)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public AgencyResponse updateAgency(Long agencyId, AgencyCreateRequest request) {
        Agency agency = agencyRepository.findById(agencyId)
                .filter(a -> a.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));

        // Validate CCN uniqueness (excluding current agency)
        if (request.getCcn() != null && !request.getCcn().equals(agency.getCcn())) {
            if (agencyRepository.existsByCcnAndIsActiveTrue(request.getCcn())) {
                throw new ValidationException("Agency with CCN " + request.getCcn() + " already exists");
            }
        }

        // Update fields
        agency.setName(request.getName());
        agency.setCcn(request.getCcn());
        agency.setAgencyType(request.getAgencyType());
        agency.setSubscriptionPlan(request.getSubscriptionPlan());
        agency.setContactEmail(request.getContactEmail());
        agency.setContactPhone(request.getContactPhone());
        agency.setAddress(request.getAddress());

        agency = agencyRepository.save(agency);

        return convertToResponse(agency);
    }

    public void deleteAgency(Long agencyId) {
        Agency agency = agencyRepository.findById(agencyId)
                .filter(a -> a.getIsActive())
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));

        // Soft delete
        agency.setIsActive(false);
        agencyRepository.save(agency);
    }

    @Transactional(readOnly = true)
    public boolean existsByIdAndActive(Long agencyId) {
        return agencyRepository.existsByIdAndActive(agencyId);
    }

    // Helper method to convert entity to response DTO
    private AgencyResponse convertToResponse(Agency agency) {
        AgencyResponse response = new AgencyResponse();
        response.setAgencyId(agency.getAgencyId());
        response.setName(agency.getName());
        response.setCcn(agency.getCcn());
        response.setAgencyType(agency.getAgencyType());
        response.setSubscriptionPlan(agency.getSubscriptionPlan());
        response.setContactEmail(agency.getContactEmail());
        response.setContactPhone(agency.getContactPhone());
        response.setAddress(agency.getAddress());
        response.setCreatedAt(agency.getCreatedAt());
        response.setUserCount(agency.getUserAssignments().size());

        return response;
    }
}