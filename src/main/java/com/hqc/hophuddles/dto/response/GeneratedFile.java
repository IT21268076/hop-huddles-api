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
public class GeneratedFile {

    private String fileType; // PDF, AUDIO, VIDEO
    private String fileName;
    private String fileUrl;
    private String contentType;
    private Long fileSize;
    private Map<String, String> metadata;
}
