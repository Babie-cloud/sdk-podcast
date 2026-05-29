package com.ngpodcast.newsletter.dto;

public record NewsletterSubscribeResponse(
        String message,
        boolean alreadySubscribed
) {}
