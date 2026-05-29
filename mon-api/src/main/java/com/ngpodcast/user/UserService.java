package com.ngpodcast.user;

import com.ngpodcast.auth.dto.AuthResponse;
import com.ngpodcast.user.dto.ProfilePatchRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public AuthResponse.UserDto patchProfile(User principal, ProfilePatchRequest body) {
        User entity = userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable."));
        entity.setUsername(body.username().trim());
        entity.setPrenom(body.prenom().trim());
        entity.setName(body.name().trim());
        userRepository.save(entity);
        return new AuthResponse.UserDto(
                entity.getId(),
                entity.getEmail(),
                entity.getPublicHandle(),
                entity.getRole().name(),
                entity.getName(),
                entity.getPrenom(),
                entity.isEmailVerified()
        );
    }

    @Transactional
    public void deleteProfile(User principal) {
        User entity = userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable."));
        userRepository.delete(entity);
    }
}
