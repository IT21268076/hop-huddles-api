package com.hqc.hophuddles.repository;

import com.hqc.hophuddles.entity.Agency;
import com.hqc.hophuddles.enums.AgencyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgencyRepository extends JpaRepository<Agency, Long> {

    // Basic finders
    List<Agency> findByIsActiveTrueOrderByNameAsc();

    Optional<Agency> findByCcnAndIsActiveTrue(String ccn);

    List<Agency> findByAgencyTypeAndIsActiveTrueOrderByNameAsc(AgencyType agencyType);

    // Paginated search with filters
    @Query("SELECT a FROM Agency a WHERE a.isActive = true " +
            "AND (:name IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:agencyType IS NULL OR a.agencyType = :agencyType) " +
            "ORDER BY a.name ASC")
    Page<Agency> findAgenciesWithFilters(
            @Param("name") String name,
            @Param("agencyType") AgencyType agencyType,
            Pageable pageable
    );

    // Existence checks
    boolean existsByCcnAndIsActiveTrue(String ccn);

    // Performance analytics
    @Query("SELECT a.agencyType, COUNT(a) FROM Agency a WHERE a.isActive = true GROUP BY a.agencyType")
    List<Object[]> countByAgencyType();

    // Multi-tenant check
    @Query("SELECT COUNT(a) > 0 FROM Agency a WHERE a.agencyId = :agencyId AND a.isActive = true")
    boolean existsByIdAndActive(@Param("agencyId") Long agencyId);
}