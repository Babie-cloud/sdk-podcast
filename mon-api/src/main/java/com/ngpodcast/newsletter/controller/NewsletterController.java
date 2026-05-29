package com.ngpodcast.newsletter.controller;

import com.ngpodcast.newsletter.dto.NewsletterSubscribeRequest;
import com.ngpodcast.newsletter.dto.NewsletterSubscribeResponse;
import com.ngpodcast.newsletter.service.NewsletterService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/newsletter")
public class NewsletterController {

    private final NewsletterService newsletterService;

    public NewsletterController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<NewsletterSubscribeResponse> subscribe(
            @Valid @RequestBody NewsletterSubscribeRequest request
    ) {
        return ResponseEntity.ok(newsletterService.subscribe(request));
    }
}
