package com.ngpodcast.component.dto;

import java.time.LocalDateTime;

public record WritingDto(
        String id,
        String title,
        String content,
        String type,
        String audioUrl,
        String coverUrl,
        String status,
        Integer views,
        String authorId,
        String authorName,
        Boolean anonymousAuthor,
        String podcastCategory,
        LocalDateTime createdAt
) {}
