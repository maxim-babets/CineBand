package com.cineband.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 100) String displayName,
        @NotBlank @Pattern(regexp = "^[a-zA-Z0-9_]{3,32}$", message = "Handle: 3–32 chars, letters, digits, underscore") String nick,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 128) String password
) {
}
