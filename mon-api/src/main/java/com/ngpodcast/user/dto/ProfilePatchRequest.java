package com.ngpodcast.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corps de {@code PATCH /users/me} — identité affichée (pseudo public + nom légal). */
public record ProfilePatchRequest(
        @NotBlank @Size(min = 1, max = 160) String username,
        @NotBlank @Size(min = 1, max = 120) String prenom,
        @NotBlank @Size(min = 1, max = 120) String name
) {}
