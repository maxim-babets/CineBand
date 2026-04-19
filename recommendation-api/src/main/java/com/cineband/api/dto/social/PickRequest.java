package com.cineband.api.dto.social;

import jakarta.validation.constraints.NotNull;

public record PickRequest(
        @NotNull Integer movieId
) {
}
