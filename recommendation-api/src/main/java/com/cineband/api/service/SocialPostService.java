package com.cineband.api.service;

import com.cineband.api.domain.ReactionType;
import com.cineband.api.domain.WallPost;
import com.cineband.api.dto.social.PostDto;
import com.cineband.api.repo.PostReactionRepository;
import com.cineband.api.repo.UserAccountRepository;
import com.cineband.api.repo.WallPostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SocialPostService {

    private final WallPostRepository wallPostRepository;
    private final PostReactionRepository postReactionRepository;
    private final UserAccountRepository userAccountRepository;

    public SocialPostService(
            WallPostRepository wallPostRepository,
            PostReactionRepository postReactionRepository,
            UserAccountRepository userAccountRepository
    ) {
        this.wallPostRepository = wallPostRepository;
        this.postReactionRepository = postReactionRepository;
        this.userAccountRepository = userAccountRepository;
    }

    public List<PostDto> listPosts(Integer viewerUserId) {
        return wallPostRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(p -> toDto(p, viewerUserId))
                .toList();
    }

    public PostDto getPostDto(int postId, Integer viewerUserId) {
        WallPost p = wallPostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        return toDto(p, viewerUserId);
    }

    private PostDto toDto(WallPost p, Integer viewerUserId) {
        var author = userAccountRepository.findById(p.getUserId())
                .orElseThrow(() -> new IllegalStateException("Missing author for post " + p.getId()));
        long likes = postReactionRepository.countByPostIdAndType(p.getId(), ReactionType.LIKE);
        long dislikes = postReactionRepository.countByPostIdAndType(p.getId(), ReactionType.DISLIKE);
        String myReaction = null;
        if (viewerUserId != null) {
            myReaction = postReactionRepository.findByPostIdAndUserId(p.getId(), viewerUserId)
                    .map(r -> r.getType().name())
                    .orElse(null);
        }
        String nick = author.getNick() != null ? author.getNick() : ("user" + author.getId());
        return new PostDto(
                p.getId(),
                nick,
                author.getDisplayName(),
                p.getContent(),
                p.getCreatedAt(),
                likes,
                dislikes,
                myReaction
        );
    }

    public WallPost createPost(Integer userId, String content) {
        WallPost p = new WallPost();
        p.setUserId(userId);
        p.setContent(content.trim());
        return wallPostRepository.save(p);
    }

    public void react(Integer postId, Integer userId, ReactionType type) {
        WallPost post = wallPostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        var existing = postReactionRepository.findByPostIdAndUserId(post.getId(), userId);
        if (existing.isPresent()) {
            if (existing.get().getType() == type) {
                postReactionRepository.delete(existing.get());
            } else {
                existing.get().setType(type);
                postReactionRepository.save(existing.get());
            }
        } else {
            var r = new com.cineband.api.domain.PostReaction();
            r.setPostId(post.getId());
            r.setUserId(userId);
            r.setType(type);
            postReactionRepository.save(r);
        }
    }
}
