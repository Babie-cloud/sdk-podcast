package com.ngpodcast.podcast.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "episodes")
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "podcast_id", nullable = false)
    private Podcast podcast;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(nullable = false)
    private Integer duration = 0;

    @Column(name = "file_size")
    private Long fileSize = 0L;

    @Column(name = "episode_number")
    private Integer episodeNumber = 1;

    @Column(nullable = false, length = 20)
    private String status = "DRAFT";

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Constructeur ──────────────────────────────────────────
    public Episode() {}

    // ── Getters / Setters ────────────────────────────────────
    public String    getId()             { return id; }
    public Podcast   getPodcast()        { return podcast; }
    public String    getTitle()          { return title; }
    public String    getDescription()    { return description; }
    public String    getAudioUrl()       { return audioUrl; }
    public Integer   getDuration()       { return duration; }
    public Long      getFileSize()       { return fileSize; }
    public Integer   getEpisodeNumber()  { return episodeNumber; }
    public String    getStatus()         { return status; }
    public LocalDateTime getPublishedAt(){ return publishedAt; }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    public void setPodcast(Podcast podcast)          { this.podcast = podcast; }
    public void setTitle(String title)               { this.title = title; }
    public void setDescription(String description)   { this.description = description; }
    public void setAudioUrl(String audioUrl)         { this.audioUrl = audioUrl; }
    public void setDuration(Integer duration)        { this.duration = duration; }
    public void setFileSize(Long fileSize)           { this.fileSize = fileSize; }
    public void setEpisodeNumber(Integer n)          { this.episodeNumber = n; }
    public void setStatus(String status)             { this.status = status; }
    public void setPublishedAt(LocalDateTime date)   { this.publishedAt = date; }
}