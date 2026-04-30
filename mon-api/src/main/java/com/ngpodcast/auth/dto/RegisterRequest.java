package com.ngpodcast.auth.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
    @NotBlank              String name,
    @NotBlank              String prenom,
    @Email @NotBlank       String email,
    @NotBlank @Size(min=6) String password
) {}
