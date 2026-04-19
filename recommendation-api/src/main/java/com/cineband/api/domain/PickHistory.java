package com.cineband.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

import jakarta.persistence.PrePersist;

@Entity
@Table(name = "HistoriaWyborow")
public class PickHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_wyboru")
    private Integer id;

    @Column(name = "id_uzytkownika", nullable = false)
    private Integer userId;

    @Column(name = "id_filmu", nullable = false)
    private Integer filmId;

    @Column(name = "moment", nullable = false)
    private Instant moment;

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

    public Instant getMoment() {
        return moment;
    }

    public void setMoment(Instant moment) {
        this.moment = moment;
    }

    @PrePersist
    void prePersist() {
        if (moment == null) {
            moment = Instant.now();
        }
    }
}
