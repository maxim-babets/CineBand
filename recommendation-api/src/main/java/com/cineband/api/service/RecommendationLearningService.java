package com.cineband.api.service;

import com.cineband.api.domain.FeedbackSentiment;
import com.cineband.api.domain.FeedbackSource;
import com.cineband.api.domain.Film;
import com.cineband.api.domain.RecommendationSignal;
import com.cineband.api.domain.UserRating;
import com.cineband.api.dto.learning.DislikedTitleDto;
import com.cineband.api.repo.FilmRepository;
import com.cineband.api.repo.RecommendationSignalRepository;
import com.cineband.api.repo.UserRatingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Bridges UI feedback into the same {@code Oceny} table the Python ML service reads on every
 * {@code /recommend} call ({@code load_ratings_and_films()}). Likes/dislikes upsert implicit ratings so KNN / NMF
 * see updated taste without a separate model — signals are also stored in {@code SygnalyRekomendacji} for auditing.
 */
@Service
public class RecommendationLearningService {

    private static final BigDecimal LIKE_SCORE = new BigDecimal("8.5");
    private static final BigDecimal DISLIKE_SCORE = new BigDecimal("2.5");

    private final RecommendationSignalRepository signalRepository;
    private final UserRatingRepository userRatingRepository;
    private final FilmRepository filmRepository;

    public RecommendationLearningService(
            RecommendationSignalRepository signalRepository,
            UserRatingRepository userRatingRepository,
            FilmRepository filmRepository
    ) {
        this.signalRepository = signalRepository;
        this.userRatingRepository = userRatingRepository;
        this.filmRepository = filmRepository;
    }

    @Transactional
    public void recordFeedback(int userId, int movieId, FeedbackSentiment sentiment, FeedbackSource source) {
        filmRepository.findById(movieId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown film"));

        RecommendationSignal sig = new RecommendationSignal();
        sig.setUserId(userId);
        sig.setFilmId(movieId);
        sig.setSentiment(sentiment);
        sig.setSource(source);
        signalRepository.save(sig);

        BigDecimal targetScore = sentiment == FeedbackSentiment.LIKE ? LIKE_SCORE : DISLIKE_SCORE;
        userRatingRepository.findFirstByUserIdAndFilmId(userId, movieId).ifPresentOrElse(r -> {
            r.setScore(targetScore);
            userRatingRepository.save(r);
        }, () -> {
            UserRating r = new UserRating();
            r.setUserId(userId);
            r.setFilmId(movieId);
            r.setScore(targetScore);
            userRatingRepository.save(r);
        });
    }

    public List<DislikedTitleDto> listDislikedTitles(int userId) {
        return signalRepository.findByUserIdAndSentimentOrderByCreatedAtDesc(userId, FeedbackSentiment.DISLIKE)
                .stream()
                .map(sig -> {
                    Film f = filmRepository.findById(sig.getFilmId())
                            .orElseThrow(() -> new IllegalStateException("Missing film " + sig.getFilmId()));
                    return new DislikedTitleDto(
                            f.getId(),
                            f.getTitle(),
                            sig.getCreatedAt(),
                            sig.getSource().name()
                    );
                })
                .toList();
    }
}
