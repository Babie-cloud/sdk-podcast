package com.ngpodcast.component.controller;

import com.ngpodcast.component.dto.CreateWritingRequest;
import com.ngpodcast.component.dto.UpdateWritingRequest;
import com.ngpodcast.component.dto.WritingDto;
import com.ngpodcast.component.service.WritingService;
import com.ngpodcast.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/writings")
public class WritingController {

    private final WritingService writingService;

    public WritingController(WritingService writingService) {
        this.writingService = writingService;
    }

    @GetMapping
    public List<WritingDto> list(@RequestParam(required = false) String q) {
        return writingService.findPublished(q);
    }

    @GetMapping("/mine")
    public List<WritingDto> mine(@AuthenticationPrincipal User user) {
        return writingService.findMine(user);
    }

    @GetMapping("/{id}")
    public WritingDto getOne(@PathVariable String id, @AuthenticationPrincipal User user) {
        return writingService.getById(id, user);
    }

    @PostMapping("/{id}/view")
    public WritingDto registerView(@PathVariable String id, @AuthenticationPrincipal User user) {
        return writingService.registerView(id, user);
    }

    @PostMapping
    public ResponseEntity<WritingDto> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateWritingRequest req
    ) {
        WritingDto dto = writingService.create(user, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    public WritingDto update(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            @Valid @RequestBody UpdateWritingRequest req
    ) {
        return writingService.update(user, id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable String id) {
        writingService.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}
