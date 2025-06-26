package com.hqc.hophuddles.repository;

import com.hqc.hophuddles.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByBranchBranchIdAndIsActiveTrueOrderByNameAsc(Long branchId);

    @Query("SELECT t FROM Team t " +
            "WHERE t.branch.agency.agencyId = :agencyId " +
            "AND t.isActive = true " +
            "ORDER BY t.branch.name, t.name")
    List<Team> findByAgencyIdAndIsActiveTrueOrderByBranchAndName(@Param("agencyId") Long agencyId);

    boolean existsByNameAndBranchBranchIdAndIsActiveTrue(String name, Long branchId);

    @Query("SELECT COUNT(t) FROM Team t " +
            "WHERE t.branch.branchId = :branchId " +
            "AND t.isActive = true")
    long countByBranch(@Param("branchId") Long branchId);

    @Query("SELECT COUNT(t) > 0 FROM Team t " +
            "WHERE t.teamId = :teamId " +
            "AND t.branch.agency.agencyId = :agencyId " +
            "AND t.isActive = true")
    boolean existsByIdAndAgency(@Param("teamId") Long teamId, @Param("agencyId") Long agencyId);
}
