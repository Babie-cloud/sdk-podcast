package com.ngpodcast.component.repository;

import com.ngpodcast.component.entity.Writing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface WritingRepository extends JpaRepository<Writing, String> {
    List<Writing> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Writing> findByTypeAndStatusOrderByCreatedAtDesc(String type, String status);
    List<Writing> findByStatusOrderByCreatedAtDesc(String status);

    @Query("""
            SELECT w FROM Writing w
            WHERE LOWER(w.title) LIKE LOWER(CONCAT('%', :query, '%'))
              AND UPPER(w.status) = 'PUBLISHED'
            ORDER BY w.createdAt DESC
            """)
    List<Writing> searchPublishedByTitle(String query);
}
