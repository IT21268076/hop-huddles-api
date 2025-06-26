package com.hqc.hophuddles.enums;

public enum GenerationStatus {
    PENDING("Generation Pending"),
    IN_PROGRESS("Generation In Progress"),
    CONTENT_GENERATED("Content Generated"),
    FILES_GENERATING("Files Generating"),
    COMPLETED("Generation Completed"),
    FAILED("Generation Failed"),
    CANCELLED("Generation Cancelled");

    private final String displayName;

    GenerationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }

    public boolean isSuccess() {
        return this == COMPLETED;
    }
}