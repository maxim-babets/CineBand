package com.cineband.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

import jakarta.persistence.PrePersist;

@Entity
@Table(name = "SygnalyRekomendacji")
public class RecommendationSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sygnalu")
    private Integer id;

    @Column(name = "id_uzytkownika", nullable = false)
    private Integer userId;

    @Column(name = "id_filmu", nullable = false)
    private Integer filmId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment", nullable = false, length = 16)
    private FeedbackSentiment sentiment;

    @Enumerated(EnumType.STRING)
    @Column(name = "zrodlo", nullable = false, length = 32)
    private FeedbackSource source;

    @Column(name = "utworzono", nullable = false)
    private Instant createdAt;

    public Integer getId() {
        return id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getFilmId() {
        return filmId;
    }

    public void setFilmId(Integer filmId) {
        this.filmId = filmId;
    }

    public FeedbackSentiment getSentiment() {
        return sentiment;
    }

    public void setSentiment(FeedbackSentiment sentiment) {
        this.sentiment = sentiment;
    }

    public FeedbackSource getSource() {
        return source;
    }

    public void setSource(FeedbackSource source) {
        this.source = source;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
