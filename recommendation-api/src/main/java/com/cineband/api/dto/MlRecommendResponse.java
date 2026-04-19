package com.cineband.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MlRecommendResponse(
        @JsonProperty("user_id") int userId,
        String method,
        List<MovieScore> recommendations
) {
}
