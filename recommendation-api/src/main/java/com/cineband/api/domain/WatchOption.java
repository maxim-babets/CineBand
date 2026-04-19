package com.cineband.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "GdzieObejrzec")
public class WatchOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opcji")
    private Integer id;

    @Column(name = "id_filmu", nullable = false)
    private Integer filmId;

    @Column(name = "provider", nullable = false, length = 128)
    private String provider;

    @Column(name = "url", nullable = false, length = 1024)
    private String url;

    public Integer getId() {
        return id;
    }

    public Integer getFilmId() {
        return filmId;
    }

    public String getProvider() {
        return provider;
    }

    public String getUrl() {
        return url;
    }
}
