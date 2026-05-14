package com.ngpodcast.component.repository;

import com.ngpodcast.component.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EpisodeRepository extends JpaRepository<Episode, String> {
    List<Episode> findByPodcastIdOrderByEpisodeNumberAsc(String podcastId);
    List<Episode> findByPodcastIdAndStatusOrderByEpisodeNumberAsc(String podcastId, String status);
    Optional<Episode> findTopByPodcastIdOrderByEpisodeNumberDesc(String podcastId);
}
