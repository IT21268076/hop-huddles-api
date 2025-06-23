package com.hqc.hophuddles.enums;

public enum HuddleType {
    INTRO("Introduction"),
    STANDARD("Standard"),
    ASSESSMENT("Assessment"),
    SUMMARY("Summary");

    private final String displayName;

    HuddleType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}