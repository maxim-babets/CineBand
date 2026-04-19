package com.cineband.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.cineband.api")
public class RecommendationApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecommendationApiApplication.class, args);
    }
}
