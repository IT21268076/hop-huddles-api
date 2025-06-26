package com.hqc.hophuddles.repository;

import com.hqc.hophuddles.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    List<Branch> findByAgencyAgencyIdAndIsActiveTrueOrderByNameAsc(Long agencyId);

    List<Branch> findByIsActiveTrueOrderByNameAsc();

    @Query("SELECT COUNT(b) FROM Branch b WHERE b.agency.agencyId = :agencyId AND b.isActive = true")
    long countByAgency(@Param("agencyId") Long agencyId);

    boolean existsByNameAndAgencyAgencyIdAndIsActiveTrue(String name, Long agencyId);

    @Query("SELECT COUNT(b) > 0 FROM Branch b " +
            "WHERE b.branchId = :branchId " +
            "AND b.agency.agencyId = :agencyId " +
            "AND b.isActive = true")
    boolean existsByIdAndAgency(@Param("branchId") Long branchId, @Param("agencyId") Long agencyId);
}
