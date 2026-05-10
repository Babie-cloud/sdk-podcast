package com.ngpodcast.component.repository;

import com.ngpodcast.component.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EpisodeRepository extends JpaRepository<Episode, String> {
    List<Episode> findByPodcastIdOrderByEpisodeNumberAsc(String podcastId);
    List<Episode> findByPodcastIdAndStatusOrderByEpisodeNumberAsc(String podcastId, String status);
    Episode findTopByPodcastIdOrderByEpisodeNumberDesc(String podcastId);
}
