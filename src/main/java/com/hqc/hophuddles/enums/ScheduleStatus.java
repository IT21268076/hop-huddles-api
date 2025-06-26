package com.hqc.hophuddles.enums;

public enum ScheduleStatus {
    ACTIVE("Active"),
    PAUSED("Paused"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled");

    private final String displayName;

    ScheduleStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}