package com.cineband.api.dto.learning;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record DislikedTitleDto(
        @JsonProperty("movie_id") int movieId,
        String title,
        Instant at,
        String source
) {
}
