package com.cineband.api.repo;

import com.cineband.api.domain.PostReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostReactionRepository extends JpaRepository<PostReaction, Integer> {

    Optional<PostReaction> findByPostIdAndUserId(Integer postId, Integer userId);

    List<PostReaction> findByPostId(Integer postId);

    long countByPostIdAndType(Integer postId, com.cineband.api.domain.ReactionType type);
}
