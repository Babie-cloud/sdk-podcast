package com.ngpodcast.component.repository;

import com.ngpodcast.component.entity.Storytelling;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StorytellingRepository extends JpaRepository<Storytelling, String> {
    List<Storytelling> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Storytelling> findByTypeAndStatusOrderByCreatedAtDesc(String type, String status);
    List<Storytelling> findByStatusOrderByCreatedAtDesc(String status);
    List<Storytelling> findByStatusAndAnonymousFalseOrderByCreatedAtDesc(String status);
    List<Storytelling> findByAnonymousTrueAndStatusOrderByCreatedAtDesc(String status);
}
