package com.ngpodcast.component.entity;

import com.ngpodcast.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "writings")
public class Writing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 20)
    private String type = "POEM";

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(nullable = false, length = 20)
    private String status = "DRAFT";

    @Column(nullable = false)
    private Integer views = 0;

    @Column(name = "anonymous_author", nullable = false)
    private boolean anonymousAuthor = false;

    @Column(name = "podcast_category", length = 80)
    private String podcastCategory;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Writing() {}

    public String getId()               { return id; }
    public User getUser()               { return user; }
    public String getTitle()            { return title; }
    public String getContent()          { return content; }
    public String getType()             { return type; }
    public String getAudioUrl()         { return audioUrl; }
    public String getCoverUrl()         { return coverUrl; }
    public String getStatus()           { return status; }
    public Integer getViews()           { return views; }
    public boolean isAnonymousAuthor()  { return anonymousAuthor; }
    public String getPodcastCategory()  { return podcastCategory; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setUser(User user)           { this.user = user; }
    public void setTitle(String title)       { this.title = title; }
    public void setContent(String content)   { this.content = content; }
    public void setType(String type)         { this.type = type; }
    public void setAudioUrl(String url)      { this.audioUrl = url; }
    public void setCoverUrl(String url)      { this.coverUrl = url; }
    public void setStatus(String status)     { this.status = status; }
    public void setViews(Integer views)      { this.views = views; }
    public void setAnonymousAuthor(boolean anonymousAuthor) { this.anonymousAuthor = anonymousAuthor; }
    public void setPodcastCategory(String podcastCategory)  { this.podcastCategory = podcastCategory; }

    @PreUpdate
    public void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
