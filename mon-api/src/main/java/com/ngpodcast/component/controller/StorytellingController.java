package com.ngpodcast.component.controller;

import com.ngpodcast.component.dto.CreateStorytellingRequest;
import com.ngpodcast.component.dto.StorytellingDto;
import com.ngpodcast.component.service.StorytellingService;
import com.ngpodcast.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/storytellings")
public class StorytellingController {

    private final StorytellingService storytellingService;

    public StorytellingController(StorytellingService storytellingService) {
        this.storytellingService = storytellingService;
    }

    @GetMapping
    public List<StorytellingDto> list() {
        return storytellingService.findPublished();
    }

    @GetMapping("/mine")
    public List<StorytellingDto> mine(@AuthenticationPrincipal User user) {
        return storytellingService.findMine(user);
    }

    @GetMapping("/{id}")
    public StorytellingDto getOne(@PathVariable String id, @AuthenticationPrincipal User user) {
        return storytellingService.getById(id, user);
    }

    @PostMapping
    public ResponseEntity<StorytellingDto> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateStorytellingRequest req
    ) {
        StorytellingDto dto = storytellingService.create(user, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable String id) {
        storytellingService.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}
