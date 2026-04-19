package com.cineband.api.service;

import com.cineband.api.dto.MlRecommendRequest;
import com.cineband.api.dto.MlRecommendResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class RecommendationService {

    private final RestTemplate mlRestTemplate;
    private final String mlBaseUrl;

    public RecommendationService(
            RestTemplate mlRestTemplate,
            @Value("${ml.service.base-url}") String mlBaseUrl
    ) {
        this.mlRestTemplate = mlRestTemplate;
        this.mlBaseUrl = mlBaseUrl.endsWith("/") ? mlBaseUrl.substring(0, mlBaseUrl.length() - 1) : mlBaseUrl;
    }

    public MlRecommendResponse getRecommendations(int userId, String method, int limit, int kNeighbors) {
        var body = new MlRecommendRequest(userId, method, limit, kNeighbors);
        try {
            MlRecommendResponse res = mlRestTemplate.postForObject(
                    mlBaseUrl + "/recommend", body, MlRecommendResponse.class);
            if (res == null) {
                throw new MlServiceException("ML service returned empty body", null);
            }
            return res;
        } catch (RestClientException e) {
            throw new MlServiceException("ML service request failed: " + e.getMessage(), e);
        }
    }

    public static final class MlServiceException extends RuntimeException {
        public MlServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
