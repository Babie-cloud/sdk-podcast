package com.ngpodcast.user;

import com.ngpodcast.auth.dto.AuthResponse;
import com.ngpodcast.user.dto.ProfilePatchRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserDto> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(toDto(user));
    }

    @PatchMapping("/me")
    public ResponseEntity<AuthResponse.UserDto> patchMe(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ProfilePatchRequest body
    ) {
        return ResponseEntity.ok(userService.patchProfile(user, body));
    }

    private static AuthResponse.UserDto toDto(User user) {
        return new AuthResponse.UserDto(
                user.getId(),
                user.getEmail(),
                user.getPublicHandle(),
                user.getRole().name(),
                user.getName(),
                user.getPrenom()
        );
    }
}
