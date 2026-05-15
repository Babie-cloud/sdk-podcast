package com.ngpodcast.component.dto;

public record EpisodePatchRequest(
        Boolean publishNow,
        String title,
        String description
) {}
