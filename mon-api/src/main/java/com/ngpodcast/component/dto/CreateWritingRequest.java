package com.ngpodcast.component.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateWritingRequest(
        @NotBlank String title,
        @NotBlank String content,
        String type,
        String status,
        String audioUrl,
        String coverUrl
) {}
