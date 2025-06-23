package com.hqc.hophuddles.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

// Supporting class for content validation
@Data
public class ContentValidationResult {
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private boolean isValid = true;

    public void addError(String error) {
        errors.add(error);
        isValid = false;
    }

    public void addWarning(String warning) {
        warnings.add(warning);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
}