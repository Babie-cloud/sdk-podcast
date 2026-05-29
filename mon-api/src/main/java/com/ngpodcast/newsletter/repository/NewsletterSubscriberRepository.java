package com.ngpodcast.newsletter.repository;

import com.ngpodcast.newsletter.entity.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, String> {
    Optional<NewsletterSubscriber> findByEmailIgnoreCase(String email);
}
