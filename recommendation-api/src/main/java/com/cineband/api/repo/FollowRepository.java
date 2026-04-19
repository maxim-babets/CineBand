package com.cineband.api.repo;

import com.cineband.api.domain.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Integer> {

    long countByFollowingId(Integer userId);

    long countByFollowerId(Integer userId);

    boolean existsByFollowerIdAndFollowingId(Integer followerId, Integer followingId);

    Optional<Follow> findByFollowerIdAndFollowingId(Integer followerId, Integer followingId);

    void deleteByFollowerIdAndFollowingId(Integer followerId, Integer followingId);
}
