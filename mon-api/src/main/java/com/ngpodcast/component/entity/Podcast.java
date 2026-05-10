package com.ngpodcast.podcast.entity;

import com.ngpodcast.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "podcasts")
public class Podcast {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(length = 100)
    private String category;

    @Column(length = 10)
    private String language = "fr";

    @Column(nullable = false, length = 20)
    private String status = "DRAFT";

    @OneToMany(mappedBy = "podcast", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Episode> episodes = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ── Constructeur ──────────────────────────────────────────
    public Podcast() {}

    // ── Getters / Setters ────────────────────────────────────
    public String getId()                  { return id; }
    public User   getUser()                { return user; }
    public String getTitle()               { return title; }
    public String getDescription()         { return description; }
    public String getCoverUrl()            { return coverUrl; }
    public String getCategory()            { return category; }
    public String getLanguage()            { return language; }
    public String getStatus()              { return status; }
    public List<Episode> getEpisodes()     { return episodes; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
    public LocalDateTime getUpdatedAt()    { return updatedAt; }

    public void setUser(User user)                  { this.user = user; }
    public void setTitle(String title)              { this.title = title; }
    public void setDescription(String description)  { this.description = description; }
    public void setCoverUrl(String coverUrl)         { this.coverUrl = coverUrl; }
    public void setCategory(String category)         { this.category = category; }
    public void setLanguage(String language)         { this.language = language; }
    public void setStatus(String status)             { this.status = status; }

    @PreUpdate
    public void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}