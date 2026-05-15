package com.ngpodcast.component.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PodcastDetailDto(
        String id,
        String title,
        String description,
        String coverUrl,
        String authorId,
        String authorName,
        String status,
        LocalDateTime createdAt,
        String category,
        String language,
        List<EpisodeDto> episodes
) {}
