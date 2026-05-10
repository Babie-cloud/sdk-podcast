package com.ngpodcast.component.repository;

import com.ngpodcast.component.entity.Podcast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PodcastRepository extends JpaRepository<Podcast, String> {
    List<Podcast> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Podcast> findByStatusOrderByCreatedAtDesc(String status);
    long countByUserId(String userId);

    @Query("SELECT p FROM Podcast p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Podcast> searchByTitle(String query);
}
