package com.hqc.hophuddles.service;

import com.hqc.hophuddles.dto.request.BranchCreateRequest;
import com.hqc.hophuddles.dto.response.BranchResponse;
import com.hqc.hophuddles.entity.Agency;
import com.hqc.hophuddles.entity.Branch;
import com.hqc.hophuddles.exception.ResourceNotFoundException;
import com.hqc.hophuddles.repository.AgencyRepository;
import com.hqc.hophuddles.repository.BranchRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class BranchService {

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private AgencyRepository agencyRepository;

    public BranchResponse createBranch(BranchCreateRequest request) {
        Agency agency = agencyRepository.findById(request.getAgencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Agency", request.getAgencyId()));

        Branch branch = new Branch(agency, request.getName());
        branch.setLocation(request.getLocation());
        branch = branchRepository.save(branch);

        return convertToResponse(branch);
    }

    public List<BranchResponse> getBranchesByAgency(Long agencyId) {
        return branchRepository.findByAgencyAgencyIdAndIsActiveTrueOrderByNameAsc(agencyId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    private BranchResponse convertToResponse(Branch branch) {
        BranchResponse response = new BranchResponse();
        response.setBranchId(branch.getBranchId());
        response.setName(branch.getName());
        response.setLocation(branch.getLocation());
        response.setAgencyId(branch.getAgency().getAgencyId());
        response.setAgencyName(branch.getAgency().getName());
        return response;
    }
}
