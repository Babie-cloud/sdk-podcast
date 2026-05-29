package com.ngpodcast.newsletter.service;

import com.ngpodcast.newsletter.dto.NewsletterSubscribeRequest;
import com.ngpodcast.newsletter.dto.NewsletterSubscribeResponse;
import com.ngpodcast.newsletter.entity.NewsletterSubscriber;
import com.ngpodcast.newsletter.repository.NewsletterSubscriberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class NewsletterService {

    private final NewsletterSubscriberRepository repository;

    public NewsletterService(NewsletterSubscriberRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public NewsletterSubscribeResponse subscribe(NewsletterSubscribeRequest request) {
        String email = request.email().trim().toLowerCase();

        return repository.findByEmailIgnoreCase(email)
                .map(existing -> handleExisting(existing, email))
                .orElseGet(() -> createNew(email));
    }

    private NewsletterSubscribeResponse handleExisting(NewsletterSubscriber existing, String email) {
        if (existing.isActive()) {
            return new NewsletterSubscribeResponse(
                    "Cette adresse est deja inscrite a la newsletter.",
                    true
            );
        }

        existing.setActive(true);
        existing.setSubscribedAt(LocalDateTime.now());
        repository.save(existing);
        return new NewsletterSubscribeResponse(
                "Merci ! Votre inscription a la newsletter est confirmee.",
                false
        );
    }

    private NewsletterSubscribeResponse createNew(String email) {
        NewsletterSubscriber subscriber = new NewsletterSubscriber();
        subscriber.setEmail(email);
        subscriber.setActive(true);
        subscriber.setSubscribedAt(LocalDateTime.now());
        repository.save(subscriber);
        return new NewsletterSubscribeResponse(
                "Merci ! Votre inscription a la newsletter est confirmee.",
                false
        );
    }
}
