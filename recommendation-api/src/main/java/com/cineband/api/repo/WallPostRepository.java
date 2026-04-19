package com.cineband.api.repo;

import com.cineband.api.domain.WallPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WallPostRepository extends JpaRepository<WallPost, Integer> {

    List<WallPost> findAllByOrderByCreatedAtDesc();

    long countByUserId(Integer userId);
}
