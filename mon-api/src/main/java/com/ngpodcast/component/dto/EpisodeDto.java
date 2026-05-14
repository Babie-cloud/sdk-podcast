package com.ngpodcast.component.dto;

import java.time.LocalDateTime;

public record EpisodeDto(
        String id,
        String title,
        String audioUrl,
        int duration,
        String podcastId,
        LocalDateTime createdAt
) {}
