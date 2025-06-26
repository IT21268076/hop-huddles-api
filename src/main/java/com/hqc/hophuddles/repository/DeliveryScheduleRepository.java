package com.hqc.hophuddles.repository;

import com.hqc.hophuddles.entity.DeliverySchedule;
import com.hqc.hophuddles.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeliveryScheduleRepository extends JpaRepository<DeliverySchedule, Long> {

    @Query("SELECT ds FROM DeliverySchedule ds " +
            "WHERE ds.scheduleStatus = 'ACTIVE' " +
            "AND ds.isActive = true " +
            "AND ds.nextExecutionTime <= :currentTime " +
            "ORDER BY ds.nextExecutionTime ASC")
    List<DeliverySchedule> findSchedulesReadyForExecution(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT ds FROM DeliverySchedule ds " +
            "WHERE ds.sequence.sequenceId = :sequenceId " +
            "AND ds.isActive = true")
    List<DeliverySchedule> findBySequenceId(@Param("sequenceId") Long sequenceId);

    @Query("SELECT ds FROM DeliverySchedule ds " +
            "WHERE ds.sequence.agency.agencyId = :agencyId " +
            "AND ds.isActive = true " +
            "ORDER BY ds.nextExecutionTime ASC")
    List<DeliverySchedule> findByAgencyId(@Param("agencyId") Long agencyId);

    @Query("SELECT ds FROM DeliverySchedule ds " +
            "WHERE ds.scheduleStatus = :status " +
            "AND ds.isActive = true")
    List<DeliverySchedule> findByStatus(@Param("status") ScheduleStatus status);

    @Query("SELECT COUNT(ds) FROM DeliverySchedule ds " +
            "WHERE ds.sequence.agency.agencyId = :agencyId " +
            "AND ds.scheduleStatus = 'ACTIVE' " +
            "AND ds.isActive = true")
    long countActiveSchedulesByAgency(@Param("agencyId") Long agencyId);
}