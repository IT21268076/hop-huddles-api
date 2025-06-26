package com.hqc.hophuddles.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InteractiveElement {

    private String elementType; // SCENARIO, CHECKLIST, SIMULATION
    private String title;
    private String description;
    private Map<String, Object> configuration;
}
