package com.cineband.api.web;

import com.cineband.api.auth.UserPrincipal;
import com.cineband.api.domain.FeedbackSentiment;
import com.cineband.api.domain.FeedbackSource;
import com.cineband.api.domain.PickHistory;
import com.cineband.api.dto.auth.UserDto;
import com.cineband.api.dto.learning.DislikedTitleDto;
import com.cineband.api.dto.learning.RecommendationFeedbackRequest;
import com.cineband.api.dto.social.PickDto;
import com.cineband.api.dto.social.PickRequest;
import com.cineband.api.dto.social.ProfileCardDto;
import com.cineband.api.dto.MlRecommendResponse;
import com.cineband.api.domain.Film;
import com.cineband.api.repo.FilmRepository;
import com.cineband.api.repo.PickHistoryRepository;
import com.cineband.api.service.RecommendationLearningService;
import com.cineband.api.service.RecommendationService;
import com.cineband.api.service.SocialGraphService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final RecommendationService recommendationService;
    private final PickHistoryRepository pickHistoryRepository;
    private final FilmRepository filmRepository;
    private final RecommendationLearningService recommendationLearningService;
    private final SocialGraphService socialGraphService;

    public MeController(
            RecommendationService recommendationService,
            PickHistoryRepository pickHistoryRepository,
            FilmRepository filmRepository,
            RecommendationLearningService recommendationLearningService,
            SocialGraphService socialGraphService
    ) {
        this.recommendationService = recommendationService;
        this.pickHistoryRepository = pickHistoryRepository;
        this.filmRepository = filmRepository;
        this.recommendationLearningService = recommendationLearningService;
        this.socialGraphService = socialGraphService;
    }

    @GetMapping
    public UserDto me(@AuthenticationPrincipal UserPrincipal principal) {
        return new UserDto(
                principal.getId(),
                principal.getUsername(),
                principal.getNick() != null ? principal.getNick() : "",
                principal.getDisplayName()
        );
    }

    /**
     * Personalized ML recommendations — requires login; uses your account id in the rating matrix.
     */
    @GetMapping("/recommendations")
    public MlRecommendResponse myRecommendations(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "hybrid") String method,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "5") int kNeighbors
    ) {
        try {
            return recommendationService.getRecommendations(principal.getId(), method, limit, kNeighbors);
        } catch (RecommendationService.MlServiceException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "ML service unavailable");
        }
    }

    @GetMapping("/picks")
    public List<PickDto> myPicks(@AuthenticationPrincipal UserPrincipal principal) {
        return pickHistoryRepository.findByUserIdOrderByMomentDesc(principal.getId()).stream()
                .map(this::toPickDto)
                .toList();
    }

    /**
     * Teach the system from a recommendation context (surprise duel or ML list). Updates {@code Oceny} for the next ML run.
     */
    @PostMapping("/feedback")
    public void recommendationFeedback(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RecommendationFeedbackRequest body
    ) {
        recommendationLearningService.recordFeedback(
                principal.getId(),
                body.movieId(),
                FeedbackSentiment.valueOf(body.sentiment()),
                FeedbackSource.valueOf(body.source())
        );
    }

    @GetMapping("/feedback/dislikes")
    public List<DislikedTitleDto> feedbackDislikes(@AuthenticationPrincipal UserPrincipal principal) {
        return recommendationLearningService.listDislikedTitles(principal.getId());
    }

    @GetMapping("/profile-card")
    public ProfileCardDto myProfileCard(@AuthenticationPrincipal UserPrincipal principal) {
        return socialGraphService.buildProfileCard(principal.getId(), principal.getId());
    }

    @PostMapping("/follow/{nick}")
    public void follow(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String nick) {
        socialGraphService.follow(principal.getId(), nick);
    }

    @DeleteMapping("/follow/{nick}")
    public void unfollow(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String nick) {
        socialGraphService.unfollow(principal.getId(), nick);
    }

    @PostMapping("/picks")
    public PickDto addPick(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PickRequest body
    ) {
        Film film = filmRepository.findById(body.movieId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown film"));
        PickHistory h = new PickHistory();
        h.setUserId(principal.getId());
        h.setFilmId(film.getId());
        pickHistoryRepository.save(h);
        return toPickDto(h);
    }

    private PickDto toPickDto(PickHistory h) {
        Film f = filmRepository.findById(h.getFilmId())
                .orElseThrow(() -> new IllegalStateException("Missing film " + h.getFilmId()));
        return new PickDto(h.getId(), f.getId(), f.getTitle(), h.getMoment());
    }
}
