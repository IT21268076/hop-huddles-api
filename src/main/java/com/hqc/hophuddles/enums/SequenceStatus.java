package com.hqc.hophuddles.enums;

public enum SequenceStatus {
    DRAFT("Draft"),
    GENERATING("Generating"),
    REVIEW("Review"),
    PUBLISHED("Published"),
    ARCHIVED("Archived");

    private final String displayName;

    SequenceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canEdit() {
        return this == DRAFT || this == REVIEW;
    }

    public boolean canPublish() {
        return this == REVIEW;
    }

    public boolean isActive() {
        return this == PUBLISHED;
    }
}