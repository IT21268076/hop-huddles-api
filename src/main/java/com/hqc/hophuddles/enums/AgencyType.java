package com.hqc.hophuddles.enums;

public enum AgencyType {
    HOME_HEALTH("Home Health"),
    HOME_CARE("Home Care"),
    HOSPICE("Hospice"),
    SKILLED_NURSING("Skilled Nursing"),
    OTHER("Other");

    private final String displayName;

    AgencyType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}