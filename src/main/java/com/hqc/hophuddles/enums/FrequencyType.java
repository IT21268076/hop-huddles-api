package com.hqc.hophuddles.enums;

public enum FrequencyType {
    IMMEDIATE("Immediate"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    CUSTOM("Custom");

    private final String displayName;

    FrequencyType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}