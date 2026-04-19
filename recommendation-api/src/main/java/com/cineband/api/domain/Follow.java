package com.cineband.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

import jakarta.persistence.PrePersist;

@Entity
@Table(
        name = "Obserwuje",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_obserwujacy", "id_obserwowany"})
)
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_obserwacji")
    private Integer id;

    @Column(name = "id_obserwujacy", nullable = false)
    private Integer followerId;

    @Column(name = "id_obserwowany", nullable = false)
    private Integer followingId;

    @Column(name = "utworzono", nullable = false)
    private Instant createdAt;

    public Integer getId() {
        return id;
    }

    public Integer getFollowerId() {
        return followerId;
    }

    public void setFollowerId(Integer followerId) {
        this.followerId = followerId;
    }

    public Integer getFollowingId() {
        return followingId;
    }

    public void setFollowingId(Integer followingId) {
        this.followingId = followingId;
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
