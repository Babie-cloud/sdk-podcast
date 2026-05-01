package com.ngpodcast.user;

import com.ngpodcast.auth.dto.AuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserDto> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new AuthResponse.UserDto(
            user.getId(),
            user.getEmail(),
            user.getUsername(),
            user.getRole().name()
        ));
    }
}
