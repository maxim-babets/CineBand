package com.cineband.api.web;

import com.cineband.api.auth.UserPrincipal;
import com.cineband.api.domain.ReactionType;
import com.cineband.api.dto.social.CreatePostRequest;
import com.cineband.api.dto.social.PostDto;
import com.cineband.api.dto.social.ReactionRequest;
import com.cineband.api.service.SocialPostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class WallController {

    private final SocialPostService socialPostService;

    public WallController(SocialPostService socialPostService) {
        this.socialPostService = socialPostService;
    }

    @GetMapping
    public List<PostDto> list(Authentication authentication) {
        Integer viewerId = extractUserId(authentication);
        return socialPostService.listPosts(viewerId);
    }

    @PostMapping
    public PostDto create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreatePostRequest body
    ) {
        var saved = socialPostService.createPost(principal.getId(), body.content());
        return socialPostService.getPostDto(saved.getId(), principal.getId());
    }

    @PostMapping("/{postId}/reactions")
    public void react(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable int postId,
            @Valid @RequestBody ReactionRequest body
    ) {
        ReactionType type = ReactionType.valueOf(body.type());
        socialPostService.react(postId, principal.getId(), type);
    }

    private static Integer extractUserId(Authentication authentication) {
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
