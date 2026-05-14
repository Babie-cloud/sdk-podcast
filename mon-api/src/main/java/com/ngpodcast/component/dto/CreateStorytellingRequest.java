package com.ngpodcast.component.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateStorytellingRequest(
        @NotBlank String title,
        String content,
        String type,
        String status,
        Boolean anonymous,
        String audioUrl,
        String coverUrl
) {}
