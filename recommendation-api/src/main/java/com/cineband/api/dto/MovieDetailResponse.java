package com.cineband.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MovieDetailResponse(
        int id,
        String title,
        String genre,
        Integer releaseYear,
        @JsonProperty("banner_url") String bannerUrl,
        List<WatchOptionDto> whereToWatch
) {
}