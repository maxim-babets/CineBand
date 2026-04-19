package com.cineband.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "ReakcjeWpisy",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_wpisu", "id_uzytkownika"})
)
public class PostReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reakcji")
    private Integer id;

    @Column(name = "id_wpisu", nullable = false)
    private Integer postId;

    @Column(name = "id_uzytkownika", nullable = false)
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "typ", nullable = false, length = 16)
    private ReactionType type;

    public Integer getId() {
        return id;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public ReactionType getType() {
        return type;
    }

    public void setType(ReactionType type) {
        this.type = type;
    }
}
