package com.cineband.api.dto.learning;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RecommendationFeedbackRequest(
        @NotNull Integer movieId,
        @NotNull
        @Pattern(regexp = "^(LIKE|DISLIKE)$", message = "sentiment must be LIKE or DISLIKE")
        String sentiment,
        @NotNull
        @Pattern(regexp = "^(SURPRISE|ML_FEED)$", message = "source must be SURPRISE or ML_FEED")
        String source
) {
}
