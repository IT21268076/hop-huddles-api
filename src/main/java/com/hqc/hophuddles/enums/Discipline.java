package com.hqc.hophuddles.enums;

public enum Discipline {
    RN("Registered Nurse"),
    PT("Physical Therapist"),
    OT("Occupational Therapist"),
    SLP("Speech-Language Pathologist"),
    LPN("Licensed Practical Nurse"),
    HHA("Home Health Aide"),
    MSW("Medical Social Worker"),
    OTHER("Other");

    private final String displayName;

    Discipline(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}