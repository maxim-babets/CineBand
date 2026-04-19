package com.cineband.api.repo;

import com.cineband.api.domain.UserRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRatingRepository extends JpaRepository<UserRating, Integer> {

    Optional<UserRating> findFirstByUserIdAndFilmId(Integer userId, Integer filmId);
}
