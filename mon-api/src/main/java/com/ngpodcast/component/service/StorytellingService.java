package com.ngpodcast.component.service;

import com.ngpodcast.component.dto.CreateStorytellingRequest;
import com.ngpodcast.component.dto.StorytellingDto;
import com.ngpodcast.component.dto.UpdateStorytellingRequest;
import com.ngpodcast.component.entity.Storytelling;
import com.ngpodcast.component.repository.StorytellingRepository;
import com.ngpodcast.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class StorytellingService {

    private final StorytellingRepository storytellingRepository;
    private final JdbcTemplate jdbcTemplate;

    public StorytellingService(StorytellingRepository storytellingRepository, JdbcTemplate jdbcTemplate) {
        this.storytellingRepository = storytellingRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<StorytellingDto> findPublished() {
        return storytellingRepository.findByStatusOrderByCreatedAtDesc("PUBLISHED").stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StorytellingDto> findMine(User user) {
        return storytellingRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public StorytellingDto getById(String id, User viewer) {
        Storytelling st = storytellingRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Introuvable."));
        if (!canRead(st, viewer)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé.");
        }
        return toDto(st);
    }

    @Transactional
    public StorytellingDto registerView(String id, User viewer) {
        Storytelling st = storytellingRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Introuvable."));
        if (!"PUBLISHED".equalsIgnoreCase(st.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seules les histoires publiques comptent les vues.");
        }
        int inserted = jdbcTemplate.update(
                """
                INSERT INTO storytelling_account_views (storytelling_id, user_id)
                VALUES (?, ?)
                ON CONFLICT DO NOTHING
                """,
                id,
                viewer.getId()
        );
        if (inserted > 0) {
            Integer current = st.getViews() != null ? st.getViews() : 0;
            st.setViews(current + 1);
            st = storytellingRepository.save(st);
        }
        return toDto(st);
    }

    @Transactional
    public StorytellingDto create(User user, CreateStorytellingRequest req) {
        Storytelling st = new Storytelling();
        st.setUser(user);
        st.setTitle(req.title().trim());
        st.setContent(req.content() != null ? req.content() : "");
        st.setType(req.type() != null && !req.type().isBlank() ? req.type().trim().toUpperCase() : "TESTIMONY");
        st.setStatus(normalizeStorytellingStatus(req.status(), "PUBLISHED"));
        boolean anon = Boolean.TRUE.equals(req.anonymous());
        st.setAnonymous(anon);
        if (req.audioUrl() != null && !req.audioUrl().isBlank()) {
            st.setAudioUrl(req.audioUrl().trim());
        }
        if (req.coverUrl() != null && !req.coverUrl().isBlank()) {
            st.setCoverUrl(req.coverUrl().trim());
        }
        Storytelling saved = storytellingRepository.save(st);
        return toDto(saved);
    }

    @Transactional
    public StorytellingDto update(User user, String id, UpdateStorytellingRequest req) {
        Storytelling st = storytellingRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Introuvable."));
        User owner = st.getUser();
        if (owner == null || !owner.getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé.");
        }
        st.setTitle(req.title().trim());
        st.setContent(req.content() != null ? req.content() : "");
        st.setType(req.type() != null && !req.type().isBlank() ? req.type().trim().toUpperCase() : st.getType());
        if (req.status() != null && !req.status().isBlank()) {
            st.setStatus(normalizeStorytellingStatus(req.status(), st.getStatus()));
        }
        if (req.anonymous() != null) {
            st.setAnonymous(req.anonymous());
        }
        if (req.audioUrl() != null && !req.audioUrl().isBlank()) {
            st.setAudioUrl(req.audioUrl().trim());
        }
        if (req.coverUrl() != null && !req.coverUrl().isBlank()) {
            st.setCoverUrl(req.coverUrl().trim());
        }
        return toDto(storytellingRepository.save(st));
    }

    @Transactional
    public void delete(User user, String id) {
        Storytelling st = storytellingRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Introuvable."));
        User owner = st.getUser();
        if (owner == null || !owner.getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé.");
        }
        storytellingRepository.delete(st);
    }

    /**
     * Témoignage publié : visible par tous si non anonyme, ou titre + anonyme si flag.
     * Brouillon : propriétaire uniquement (user non null dans l'entité).
     */
    private boolean canRead(Storytelling st, User viewer) {
        if ("PUBLISHED".equalsIgnoreCase(st.getStatus())) {
            return true;
        }
        User owner = st.getUser();
        return owner != null && viewer != null && owner.getId().equals(viewer.getId());
    }

    private static String normalizeStorytellingStatus(String status, String defaultStatus) {
        if (status == null || status.isBlank()) {
            return defaultStatus;
        }
        return "PUBLISHED".equalsIgnoreCase(status) ? "PUBLISHED" : "DRAFT";
    }

    private StorytellingDto toDto(Storytelling st) {
        User u = st.getUser();
        Integer views = st.getViews() != null ? st.getViews() : 0;
        Integer likes = st.getLikes() != null ? st.getLikes() : 0;
        Boolean anon = st.getAnonymous() != null ? st.getAnonymous() : false;

        String authorId = (u != null && !anon) ? u.getId() : "";
        String authorName;
        if (Boolean.TRUE.equals(anon)) {
            authorName = "Anonyme";
        } else if (u != null) {
            authorName = u.getPublicHandle();
        } else {
            authorName = "Anonyme";
        }

        return new StorytellingDto(
                st.getId(),
                st.getTitle(),
                st.getContent(),
                st.getType(),
                st.getAudioUrl(),
                st.getCoverUrl(),
                anon,
                st.getStatus(),
                views,
                likes,
                authorId,
                authorName,
                st.getCreatedAt()
        );
    }
}
