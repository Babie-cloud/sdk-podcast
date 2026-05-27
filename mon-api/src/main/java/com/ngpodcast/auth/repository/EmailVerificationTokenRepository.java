package com.ngpodcast.auth.repository;

import com.ngpodcast.auth.entity.EmailVerificationToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, String> {
    Optional<EmailVerificationToken> findByTokenAndUsedAtIsNullAndExpiresAtAfter(
            String token,
            Instant now
    );

    @Modifying
    @Query("update EmailVerificationToken t set t.usedAt = :now where t.user.id = :userId and t.usedAt is null")
    void markUnusedByUserIdAsUsed(@Param("userId") String userId, @Param("now") Instant now);
}
