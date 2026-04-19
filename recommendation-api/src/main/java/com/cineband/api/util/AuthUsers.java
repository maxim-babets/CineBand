package com.cineband.api.util;

import com.cineband.api.auth.UserPrincipal;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

public final class AuthUsers {

    private AuthUsers() {
    }

    /**
     * @return user id when authenticated with JWT, otherwise null
     */
    public static Integer currentUserId(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Object p = authentication.getPrincipal();
        if (p instanceof UserPrincipal up) {
            return up.getId();
        }
        return null;
    }
}
