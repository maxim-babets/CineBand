package com.cineband.api.dto.social;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Instagram-style profile header: counts + follow state.
 */
public record ProfileCardDto(
        String nick,
        @JsonProperty("display_name") String displayName,
        @JsonProperty("follower_count") long followerCount,
        @JsonProperty("following_count") long followingCount,
        @JsonProperty("posts_count") long postsCount,
        @JsonProperty("picks_count") long picksCount,
        /** Present only when the viewer is authenticated; null for anonymous viewers. */
        @JsonProperty("is_following") Boolean isFollowing,
        @JsonProperty("is_self") boolean isSelf
) {
}
