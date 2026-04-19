package com.cineband.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MovieScore(
        @JsonProperty("movieId") int movieId,
        String title,
        double score
) {
}
