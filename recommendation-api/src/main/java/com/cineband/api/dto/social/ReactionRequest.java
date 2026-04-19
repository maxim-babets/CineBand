package com.cineband.api.dto.social;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ReactionRequest(
        @NotBlank
        @Pattern(regexp = "^(LIKE|DISLIKE)$")
        String type
) {
}
