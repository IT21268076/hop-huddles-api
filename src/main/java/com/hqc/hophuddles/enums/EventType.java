package com.hqc.hophuddles.enums;

import lombok.Getter;

@Getter
public enum EventType {
    VIEW("View"),
    DOWNLOAD("Download"),
    PLAY_AUDIO("Play Audio"),
    PAUSE_AUDIO("Pause Audio"),
    ASSESSMENT_START("Assessment Start"),
    ASSESSMENT_SUBMIT("Assessment Submit"),
    FEEDBACK_SUBMIT("Feedback Submit"),
    SEQUENCE_START("Sequence Start"),
    SEQUENCE_COMPLETE("Sequence Complete");

    private final String displayName;

    EventType(String displayName) {
        this.displayName = displayName;
    }

    public boolean isEngagementEvent() {
        return this == VIEW || this == PLAY_AUDIO || this == DOWNLOAD;
    }

    public boolean isAssessmentEvent() {
        return this == ASSESSMENT_START || this == ASSESSMENT_SUBMIT;
    }
}