package com.cineband.api.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserDto(
        int id,
        String email,
        String nick,
        @JsonProperty("display_name") String displayName
) {
}
