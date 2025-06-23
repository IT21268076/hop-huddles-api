// BulkHuddleCreationRequest.java
package com.hqc.hophuddles.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkHuddleCreationRequest {

    @NotEmpty(message = "At least one huddle is required")
    @Size(max = 20, message = "Cannot create more than 20 huddles at once")
    @Valid
    private List<BulkHuddleItem> huddles;

    private Long templateId;

    private Boolean publishImmediately = false;

    private Boolean overwriteExisting = false; // Overwrite huddles with same order index

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BulkHuddleItem {

        @NotBlank(message = "Title is required")
        private String title;

        private String topic;

        @Min(value = 1, message = "Order index must be at least 1")
        private Integer orderIndex;

        @Valid
        @NotNull(message = "Content is required")
        private ManualContentRequest content;
    }
}