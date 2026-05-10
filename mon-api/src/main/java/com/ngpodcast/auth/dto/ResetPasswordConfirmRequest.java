package com.ngpodcast.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordConfirmRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 6) String newPassword
) {}
