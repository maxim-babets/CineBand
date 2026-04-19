package com.cineband.api.dto.social;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record PickDto(
        int id,
        @JsonProperty("movie_id") int movieId,
        String title,
        Instant moment
) {
}
