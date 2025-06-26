package com.hqc.hophuddles.controller;

import com.hqc.hophuddles.entity.DeliverySchedule;
import com.hqc.hophuddles.enums.Permission;
import com.hqc.hophuddles.security.RequirePermission;
import com.hqc.hophuddles.service.HuddleSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedules")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ScheduleController {

    private final HuddleSchedulerService schedulerService;

    @PostMapping("/sequence/{sequenceId}")
    @RequirePermission(value = Permission.SCHEDULE_HUDDLES, resourceIdParam = "sequenceId", resourceType = "SEQUENCE")
    public ResponseEntity<DeliverySchedule> createSchedule(
            @PathVariable Long sequenceId,
            @RequestBody DeliverySchedule scheduleRequest) {
        DeliverySchedule schedule = schedulerService.createSchedule(sequenceId, scheduleRequest);
        return ResponseEntity.ok(schedule);
    }

    @PutMapping("/{scheduleId}")
    @RequirePermission(value = Permission.SCHEDULE_HUDDLES, resourceIdParam = "scheduleId", resourceType = "SCHEDULE")
    public ResponseEntity<DeliverySchedule> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody DeliverySchedule scheduleUpdate) {
        DeliverySchedule schedule = schedulerService.updateSchedule(scheduleId, scheduleUpdate);
        return ResponseEntity.ok(schedule);
    }

    @PostMapping("/{scheduleId}/pause")
    @RequirePermission(value = Permission.SCHEDULE_HUDDLES, resourceIdParam = "scheduleId", resourceType = "SCHEDULE")
    public ResponseEntity<Void> pauseSchedule(@PathVariable Long scheduleId) {
        schedulerService.pauseSchedule(scheduleId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{scheduleId}/resume")
    @RequirePermission(value = Permission.SCHEDULE_HUDDLES, resourceIdParam = "scheduleId", resourceType = "SCHEDULE")
    public ResponseEntity<Void> resumeSchedule(@PathVariable Long scheduleId) {
        schedulerService.resumeSchedule(scheduleId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{scheduleId}")
    @RequirePermission(value = Permission.SCHEDULE_HUDDLES, resourceIdParam = "scheduleId", resourceType = "SCHEDULE")
    public ResponseEntity<Void> cancelSchedule(@PathVariable Long scheduleId) {
        schedulerService.cancelSchedule(scheduleId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sequence/{sequenceId}")
    @RequirePermission(value = Permission.VIEW_DETAILED_ANALYTICS, resourceIdParam = "sequenceId", resourceType = "SEQUENCE")
    public ResponseEntity<List<DeliverySchedule>> getSequenceSchedules(@PathVariable Long sequenceId) {
        List<DeliverySchedule> schedules = schedulerService.getSequenceSchedules(sequenceId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/agency/{agencyId}")
    @RequirePermission(value = Permission.VIEW_AGENCY_ANALYTICS, resourceIdParam = "agencyId", resourceType = "AGENCY")
    public ResponseEntity<List<DeliverySchedule>> getAgencySchedules(@PathVariable Long agencyId) {
        List<DeliverySchedule> schedules = schedulerService.getAgencySchedules(agencyId);
        return ResponseEntity.ok(schedules);
    }
}