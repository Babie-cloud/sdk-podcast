package com.ngpodcast.component.dto;

public record PodcastPatchRequest(
        String title,
        String description,
        String status,
        String category,
        String language
) {}
