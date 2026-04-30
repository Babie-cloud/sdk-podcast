package com.ngpodcast.auth.dto;

public record AuthResponse(
    String  token,
    UserDto user
) {
    public record UserDto(
        String id,
        String email,
        String username,
        String role
    ) {}
}
