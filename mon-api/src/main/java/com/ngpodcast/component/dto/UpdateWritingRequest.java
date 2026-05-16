package com.ngpodcast.component.dto;

import jakarta.validation.constraints.NotBlank;

/** Corps obligatoire sur tous les champs éditables (formulaire pré-rempli côté client). */
public record UpdateWritingRequest(
        @NotBlank String title,
        @NotBlank String content,
        String type,
        String status,
        String audioUrl,
        String coverUrl,
        Boolean anonymousAuthor,
        String podcastCategory
) {}
