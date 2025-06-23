// ContentValidationResult.java (if not already created)
package com.hqc.hophuddles.dto.response;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ContentValidationResult {
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private List<String> suggestions = new ArrayList<>();
    private boolean isValid = true;
    private String validationSummary;
    private Double contentScore; // 0-100 content quality score

    public void addError(String error) {
        errors.add(error);
        isValid = false;
    }

    public void addWarning(String warning) {
        warnings.add(warning);
    }

    public void addSuggestion(String suggestion) {
        suggestions.add(suggestion);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public boolean hasSuggestions() {
        return !suggestions.isEmpty();
    }

    public String getOverallStatus() {
        if (hasErrors()) return "ERRORS";
        if (hasWarnings()) return "WARNINGS";
        return "VALID";
    }
}