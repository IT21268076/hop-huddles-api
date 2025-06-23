package com.hqc.hophuddles.enums;

import lombok.Getter;

@Getter
public enum ProgressStatus {
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    SKIPPED("Skipped");

    private final String displayName;

    ProgressStatus(String displayName) {
        this.displayName = displayName;
    }

    public boolean isActive() {
        return this == IN_PROGRESS;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public double getProgressPercentage() {
        return switch (this) {
            case NOT_STARTED -> 0.0;
            case IN_PROGRESS -> 50.0;
            case COMPLETED -> 100.0;
            case SKIPPED -> 0.0;
        };
    }
}