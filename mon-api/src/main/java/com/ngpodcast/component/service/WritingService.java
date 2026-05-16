package com.ngpodcast.component.service;

import com.ngpodcast.component.dto.CreateWritingRequest;
import com.ngpodcast.component.dto.UpdateWritingRequest;
import com.ngpodcast.component.dto.WritingDto;
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
                .map(w -> toDto(w, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WritingDto> findMine(User user) {
        return writingRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(w -> toDto(w, user))
                .toList();
    }

    @Transactional(readOnly = true)
    public WritingDto getById(String id, User viewer) {
        Writing w = writingRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Introuvable."));
        if (!canRead(w, viewer)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé.");
        }
        return toDto(w, viewer);
    }

    @Transactional
    public WritingDto create(User user, CreateWritingRequest req) {
        Writing w = new Writing();
        w.setUser(user);
        w.setTitle(req.title().trim());
        w.setContent(req.content());
        w.setType(req.type() != null && !req.type().isBlank() ? req.type().trim().toUpperCase() : "POEM");
        w.setStatus(normalizeWritingStatus(req.status()));
        if (req.audioUrl() == null || req.audioUrl().isBlank()) {
            w.setAudioUrl(null);
        } else {
            w.setAudioUrl(req.audioUrl().trim());
        }
        if (req.coverUrl() == null || req.coverUrl().isBlank()) {
            w.setCoverUrl(null);
        } else {
            w.setCoverUrl(req.coverUrl().trim());
        }
        w.setAnonymousAuthor(Boolean.TRUE.equals(req.anonymousAuthor()));
        if (req.podcastCategory() != null && !req.podcastCategory().isBlank()) {
            w.setPodcastCategory(req.podcastCategory().trim());
        }
        Writing saved = writingRepository.save(w);
        return toDto(saved, user);
    }

    @Transactional
    public WritingDto update(User user, String id, UpdateWritingRequest req) {
        Writing w = writingRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Introuvable."));
        if (!w.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé.");
        }
        w.setTitle(req.title().trim());
        w.setContent(req.content());
        w.setType(req.type() != null && !req.type().isBlank() ? req.type().trim().toUpperCase() : w.getType());
        w.setStatus(normalizeWritingStatus(req.status()));
        if (req.audioUrl() == null || req.audioUrl().isBlank()) {
            w.setAudioUrl(null);
        } else {
            w.setAudioUrl(req.audioUrl().trim());
        }
        if (req.coverUrl() == null || req.coverUrl().isBlank()) {
            w.setCoverUrl(null);
        } else {
            w.setCoverUrl(req.coverUrl().trim());
        }
        if (req.anonymousAuthor() != null) {
            w.setAnonymousAuthor(req.anonymousAuthor());
        }
        if (req.podcastCategory() != null) {
            w.setPodcastCategory(req.podcastCategory().isBlank() ? null : req.podcastCategory().trim());
        }
        return toDto(writingRepository.save(w), user);
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

    /** @param viewer null pour les listes publiques (liste « découvrir ») sans utilisateur JWT. */
    private WritingDto toDto(Writing w, User viewer) {
        User u = w.getUser();
        Integer views = w.getViews() != null ? w.getViews() : 0;
        boolean owner = viewer != null && viewer.getId().equals(u.getId());
        boolean maskAuthor =
                w.isAnonymousAuthor()
                        && "PUBLISHED".equalsIgnoreCase(w.getStatus())
                        && !owner;

        String authorId = maskAuthor ? null : u.getId();
        String authorName = maskAuthor ? "Anonyme" : u.getPublicHandle();

        return new WritingDto(
                w.getId(),
                w.getTitle(),
                w.getContent(),
                w.getType(),
                w.getAudioUrl(),
                w.getCoverUrl(),
                w.getStatus(),
                views,
                authorId,
                authorName,
                w.isAnonymousAuthor(),
                w.getPodcastCategory(),
                w.getCreatedAt()
        );
    }
}
