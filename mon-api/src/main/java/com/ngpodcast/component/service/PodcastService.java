package com.ngpodcast.component.service;

import com.ngpodcast.component.dto.*;
import com.ngpodcast.component.entity.Episode;
import com.ngpodcast.component.entity.Podcast;
import com.ngpodcast.component.repository.EpisodeRepository;
import com.ngpodcast.component.repository.PodcastRepository;
import com.ngpodcast.storage.StorageService;
import com.ngpodcast.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class PodcastService {

    private final PodcastRepository podcastRepository;
    private final EpisodeRepository episodeRepository;
    private final StorageService storageService;

    public PodcastService(
            PodcastRepository podcastRepository,
            EpisodeRepository episodeRepository,
            StorageService storageService
    ) {
        this.podcastRepository = podcastRepository;
        this.episodeRepository = episodeRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public List<PodcastSummaryDto> findPublished(String query) {
        List<Podcast> list =
                query != null && !query.isBlank()
                        ? podcastRepository.searchByTitle(query.trim())
                        : podcastRepository.findByStatusOrderByCreatedAtDesc("PUBLISHED");
        return list.stream()
                .filter(p -> "PUBLISHED".equalsIgnoreCase(p.getStatus()))
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PodcastSummaryDto> findMine(User user) {
        return podcastRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<PodcastDetailDto> findById(String id, User viewer) {
        return podcastRepository.findById(id)
                .filter(p -> canRead(p, viewer))
                .map(this::toDetail);
    }

    @Transactional
    public PodcastDetailDto create(
            User user,
            String title,
            String description,
            String category,
            String language,
            String status,
            MultipartFile cover
    ) throws IOException {
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Titre requis.");
        }
        Podcast podcast = new Podcast();
        podcast.setUser(user);
        podcast.setTitle(title.trim());
        podcast.setDescription(description != null ? description : "");
        if (category != null && !category.isBlank()) {
            podcast.setCategory(category.trim());
        }
        if (language != null && !language.isBlank()) {
            podcast.setLanguage(language.trim().toLowerCase(Locale.ROOT));
        }
        podcast.setStatus(normalizePodcastStatus(status));
        Podcast saved = podcastRepository.save(podcast);

        if (cover != null && !cover.isEmpty()) {
            String url = storageService.saveImage(cover, saved.getId());
            saved.setCoverUrl(url);
            saved = podcastRepository.save(saved);
        }

        return toDetail(saved);
    }

    @Transactional
    public PodcastDetailDto addEpisode(
            User user,
            String podcastId,
            String title,
            String description,
            MultipartFile audio,
            boolean publishNow
    ) throws IOException {
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Titre de l'épisode requis.");
        }
        Podcast podcast = podcastRepository.findById(podcastId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Podcast introuvable."));
        if (!podcast.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé.");
        }

        if (audio == null || audio.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fichier audio requis.");
        }

        Episode episode = new Episode();
        episode.setPodcast(podcast);
        episode.setTitle(title.trim());
        episode.setDescription(description != null ? description : "");
        episode.setAudioUrl(storageService.saveAudio(audio, podcastId));
        episode.setDuration(0);

        int nextNumber = episodeRepository.findTopByPodcastIdOrderByEpisodeNumberDesc(podcastId)
                .map(ep -> ep.getEpisodeNumber() + 1)
                .orElse(1);
        episode.setEpisodeNumber(nextNumber);

        String epStatus = publishNow ? "PUBLISHED" : "DRAFT";
        episode.setStatus(epStatus);
        if (publishNow) {
            episode.setPublishedAt(LocalDateTime.now());
            podcast.setStatus("PUBLISHED");
        }

        episodeRepository.save(episode);
        podcastRepository.save(podcast);

        return toDetail(podcastRepository.findById(podcastId).orElseThrow());
    }

    @Transactional
    public void delete(User user, String id) throws IOException {
        Podcast podcast = podcastRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Podcast introuvable."));
        if (!podcast.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé.");
        }

        for (Episode ep : episodeRepository.findByPodcastIdOrderByEpisodeNumberAsc(id)) {
            try {
                storageService.delete(ep.getAudioUrl());
            } catch (IOException ignored) {
                // meille effort
            }
        }
        try {
            storageService.delete(podcast.getCoverUrl());
        } catch (IOException ignored) {
            // meille effort
        }

        podcastRepository.delete(podcast);
    }

    private boolean canRead(Podcast podcast, User viewer) {
        if ("PUBLISHED".equalsIgnoreCase(podcast.getStatus())) {
            return true;
        }
        return viewer != null && viewer.getId().equals(podcast.getUser().getId());
    }

    private static String normalizePodcastStatus(String status) {
        if (status == null || status.isBlank()) {
            return "DRAFT";
        }
        return "PUBLISHED".equalsIgnoreCase(status) ? "PUBLISHED" : "DRAFT";
    }

    private PodcastSummaryDto toSummary(Podcast p) {
        User u = p.getUser();
        return new PodcastSummaryDto(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getCoverUrl(),
                u.getId(),
                u.getUsername(),
                p.getStatus(),
                p.getCreatedAt()
        );
    }

    private PodcastDetailDto toDetail(Podcast p) {
        PodcastSummaryDto s = toSummary(p);
        List<EpisodeDto> episodes = episodeRepository.findByPodcastIdOrderByEpisodeNumberAsc(p.getId()).stream()
                .map(this::toEpisodeDto)
                .toList();

        return new PodcastDetailDto(
                s.id(),
                s.title(),
                s.description(),
                s.coverUrl(),
                s.authorId(),
                s.authorName(),
                s.status(),
                s.createdAt(),
                episodes
        );
    }

    private EpisodeDto toEpisodeDto(Episode e) {
        int dur = e.getDuration() != null ? e.getDuration() : 0;
        Podcast pod = e.getPodcast();
        return new EpisodeDto(
                e.getId(),
                e.getTitle(),
                e.getAudioUrl() != null ? e.getAudioUrl() : "",
                dur,
                pod != null ? pod.getId() : "",
                e.getCreatedAt()
        );
    }
}
