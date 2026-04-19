package com.cineband.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MlRecommendRequest(
        @JsonProperty("user_id") int userId,
        String method,
        int limit,
        @JsonProperty("k_neighbors") int kNeighbors
) {
}
