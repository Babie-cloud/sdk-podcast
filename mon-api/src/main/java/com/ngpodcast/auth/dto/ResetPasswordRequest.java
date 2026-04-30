package com.ngpodcast.auth.dto;

import jakarta.validation.constraints.*;

public record ResetPasswordRequest(
    @Email @NotBlank String email
) {}
