package com.ngpodcast.component.controller;

import com.ngpodcast.component.dto.EpisodePatchRequest;
import com.ngpodcast.component.dto.PodcastDetailDto;
import com.ngpodcast.component.dto.PodcastPatchRequest;
import com.ngpodcast.component.dto.PodcastSummaryDto;
import com.ngpodcast.component.service.PodcastService;
import com.ngpodcast.user.User;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/podcasts")
public class PodcastController {

    private final PodcastService podcastService;

    public PodcastController(PodcastService podcastService) {
        this.podcastService = podcastService;
    }

    @GetMapping
    public List<PodcastSummaryDto> list(@RequestParam(required = false) String q) {
        return podcastService.findPublished(q);
    }

    @GetMapping("/mine")
    public List<PodcastSummaryDto> mine(@AuthenticationPrincipal User user) {
        return podcastService.findMine(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PodcastDetailDto> getOne(
            @PathVariable String id,
            @AuthenticationPrincipal User user
    ) {
        return podcastService.findById(id, user)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PodcastDetailDto> create(
            @AuthenticationPrincipal User user,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String language,
            @RequestParam(required = false, defaultValue = "DRAFT") String status,
            @RequestPart(name = "cover", required = false) MultipartFile cover
    ) throws IOException {
        PodcastDetailDto created = podcastService.create(user, title, description, category, language, status, cover);
        return ResponseEntity.status(201).body(created);
    }

    @PostMapping(value = "/{podcastId}/episodes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PodcastDetailDto addEpisode(
            @AuthenticationPrincipal User user,
            @PathVariable String podcastId,
            @RequestParam String title,
            @RequestParam(required = false, defaultValue = "") String description,
            @RequestParam(required = false, defaultValue = "false") boolean publishNow,
            @RequestPart("audio") MultipartFile audio
    ) throws IOException {
        return podcastService.addEpisode(user, podcastId, title, description, audio, publishNow);
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PodcastDetailDto patchPodcast(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            @Valid @RequestBody PodcastPatchRequest body
    ) {
        return podcastService.patchPodcast(user, id, body);
    }

    @PatchMapping(value = "/{podcastId}/episodes/{episodeId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PodcastDetailDto patchEpisode(
            @AuthenticationPrincipal User user,
            @PathVariable String podcastId,
            @PathVariable String episodeId,
            @Valid @RequestBody EpisodePatchRequest body
    ) {
        return podcastService.patchEpisode(user, podcastId, episodeId, body);
    }

    @DeleteMapping("/{podcastId}/episodes/{episodeId}")
    public ResponseEntity<Void> deleteEpisode(
            @AuthenticationPrincipal User user,
            @PathVariable String podcastId,
            @PathVariable String episodeId
    ) throws IOException {
        podcastService.deleteEpisode(user, podcastId, episodeId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable String id)
            throws IOException {
        podcastService.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}
