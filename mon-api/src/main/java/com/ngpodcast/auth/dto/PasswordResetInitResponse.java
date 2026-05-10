package com.ngpodcast.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PasswordResetInitResponse(String resetToken) {}
