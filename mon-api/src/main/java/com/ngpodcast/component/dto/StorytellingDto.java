package com.ngpodcast.component.dto;

import java.time.LocalDateTime;

public record StorytellingDto(
        String id,
        String title,
        String content,
        String type,
        String audioUrl,
        String coverUrl,
        Boolean anonymous,
        String status,
        Integer views,
        Integer likes,
        String authorId,
        String authorName,
        LocalDateTime createdAt
) {}
