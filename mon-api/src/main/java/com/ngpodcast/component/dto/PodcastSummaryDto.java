package com.ngpodcast.component.dto;

import java.time.LocalDateTime;

public record PodcastSummaryDto(
        String id,
        String title,
        String description,
        String coverUrl,
        String authorId,
        String authorName,
        String status,
        LocalDateTime createdAt
) {}
