package com.ngpodcast.component.service;

import com.ngpodcast.component.dto.*;
import com.ngpodcast.component.entity.Writing;
import com.ngpodcast.component.repository.WritingRepository;
import com.ngpodcast.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class WritingService {

    private final WritingRepository writingRepository;

    public WritingService(WritingRepository writingRepository) {
        this.writingRepository = writingRepository;
    }

    @Transactional(readOnly = true)
    public List<WritingDto> findPublished(String query) {
        List<Writing> list =
                query != null && !query.isBlank()
                        ? writingRepository.searchByTitle(query.trim())
                        : writingRepository.findByStatusOrderByCreatedAtDesc("PUBLISHED");
        return list.stream()
                .filter(w -> "PUBLISHED".equalsIgnoreCase(w.getStatus()))
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WritingDto> findMine(User user) {
        return writingRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public WritingDto getById(String id, User viewer) {
        Writing w = writingRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Introuvable."));
        if (!canRead(w, viewer)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé.");
        }
        return toDto(w);
    }

    @Transactional
    public WritingDto create(User user, CreateWritingRequest req) {
        Writing w = new Writing();
        w.setUser(user);
        w.setTitle(req.title().trim());
        w.setContent(req.content());
        w.setType(req.type() != null && !req.type().isBlank() ? req.type().trim().toUpperCase() : "POEM");
        w.setStatus(normalizeWritingStatus(req.status()));
        if (req.audioUrl() != null && !req.audioUrl().isBlank()) {
            w.setAudioUrl(req.audioUrl().trim());
        }
        if (req.coverUrl() != null && !req.coverUrl().isBlank()) {
            w.setCoverUrl(req.coverUrl().trim());
        }
        Writing saved = writingRepository.save(w);
        return toDto(saved);
    }

    @Transactional
    public void delete(User user, String id) {
        Writing w = writingRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Introuvable."));
        if (!w.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé.");
        }
        writingRepository.delete(w);
    }

    private boolean canRead(Writing writing, User viewer) {
        if ("PUBLISHED".equalsIgnoreCase(writing.getStatus())) {
            return true;
        }
        return viewer != null && viewer.getId().equals(writing.getUser().getId());
    }

    private static String normalizeWritingStatus(String status) {
        if (status == null || status.isBlank()) {
            return "DRAFT";
        }
        return "PUBLISHED".equalsIgnoreCase(status) ? "PUBLISHED" : "DRAFT";
    }

    private WritingDto toDto(Writing w) {
        User u = w.getUser();
        Integer views = w.getViews() != null ? w.getViews() : 0;
        return new WritingDto(
                w.getId(),
                w.getTitle(),
                w.getContent(),
                w.getType(),
                w.getAudioUrl(),
                w.getCoverUrl(),
                w.getStatus(),
                views,
                u.getId(),
                u.getUsername(),
                w.getCreatedAt()
        );
    }
}
