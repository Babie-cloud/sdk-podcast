package com.ngpodcast.component.entity;

import com.ngpodcast.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "storytelling")
public class Storytelling {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 20)
    private String type = "TESTIMONY";

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(nullable = false)
    private Boolean anonymous = false;

    @Column(nullable = false, length = 20)
    private String status = "DRAFT";

    @Column(nullable = false)
    private Integer views = 0;

    @Column(nullable = false)
    private Integer likes = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Storytelling() {}

    public String getId()               { return id; }
    public User getUser()               { return user; }
    public String getTitle()            { return title; }
    public String getContent()          { return content; }
    public String getType()             { return type; }
    public String getAudioUrl()         { return audioUrl; }
    public String getCoverUrl()         { return coverUrl; }
    public Boolean getAnonymous()       { return anonymous; }
    public String getStatus()           { return status; }
    public Integer getViews()           { return views; }
    public Integer getLikes()           { return likes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setUser(User user)             { this.user = user; }
    public void setTitle(String title)         { this.title = title; }
    public void setContent(String content)     { this.content = content; }
    public void setType(String type)           { this.type = type; }
    public void setAudioUrl(String url)        { this.audioUrl = url; }
    public void setCoverUrl(String url)        { this.coverUrl = url; }
    public void setAnonymous(Boolean a)        { this.anonymous = a; }
    public void setStatus(String status)       { this.status = status; }
    public void setViews(Integer views)        { this.views = views; }
    public void setLikes(Integer likes)        { this.likes = likes; }

    @PreUpdate
    public void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
