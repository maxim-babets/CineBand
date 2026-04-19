package com.cineband.api.dto.social;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record PostDto(
        int id,
        @JsonProperty("author_nick") String authorNick,
        @JsonProperty("author_display_name") String authorDisplayName,
        String content,
        Instant createdAt,
        long likes,
        long dislikes,
        @JsonProperty("my_reaction") String myReaction
) {
}
