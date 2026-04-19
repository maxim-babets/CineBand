package com.cineband.api.repo;

import com.cineband.api.domain.FeedbackSentiment;
import com.cineband.api.domain.RecommendationSignal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationSignalRepository extends JpaRepository<RecommendationSignal, Integer> {

    List<RecommendationSignal> findByUserIdAndSentimentOrderByCreatedAtDesc(Integer userId, FeedbackSentiment sentiment);
}
