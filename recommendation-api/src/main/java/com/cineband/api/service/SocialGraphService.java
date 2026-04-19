package com.cineband.api.service;

import com.cineband.api.domain.Follow;
import com.cineband.api.domain.UserAccount;
import com.cineband.api.dto.social.ProfileCardDto;
import com.cineband.api.repo.FollowRepository;
import com.cineband.api.repo.PickHistoryRepository;
import com.cineband.api.repo.UserAccountRepository;
import com.cineband.api.repo.WallPostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SocialGraphService {

    private final FollowRepository followRepository;
    private final UserAccountRepository userAccountRepository;
    private final WallPostRepository wallPostRepository;
    private final PickHistoryRepository pickHistoryRepository;

    public SocialGraphService(
            FollowRepository followRepository,
            UserAccountRepository userAccountRepository,
            WallPostRepository wallPostRepository,
            PickHistoryRepository pickHistoryRepository
    ) {
        this.followRepository = followRepository;
        this.userAccountRepository = userAccountRepository;
        this.wallPostRepository = wallPostRepository;
        this.pickHistoryRepository = pickHistoryRepository;
    }

    public ProfileCardDto buildProfileCard(int targetUserId, Integer viewerUserId) {
        UserAccount u = userAccountRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        long followers = followRepository.countByFollowingId(targetUserId);
        long following = followRepository.countByFollowerId(targetUserId);
        long posts = wallPostRepository.countByUserId(targetUserId);
        long picks = pickHistoryRepository.countByUserId(targetUserId);

        boolean isSelf = viewerUserId != null && viewerUserId.equals(targetUserId);
        Boolean isFollowing = null;
        if (viewerUserId != null && !isSelf) {
            isFollowing = followRepository.existsByFollowerIdAndFollowingId(viewerUserId, targetUserId);
        }

        return new ProfileCardDto(
                u.getNick() != null ? u.getNick() : ("user" + u.getId()),
                u.getDisplayName(),
                followers,
                following,
                posts,
                picks,
                isFollowing,
                isSelf
        );
    }

    public ProfileCardDto profileByNick(String nick, Integer viewerUserId) {
        UserAccount u = userAccountRepository.findByNickIgnoreCase(nick)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return buildProfileCard(u.getId(), viewerUserId);
    }

    @Transactional
    public void follow(int followerId, String targetNick) {
        UserAccount target = userAccountRepository.findByNickIgnoreCase(targetNick)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (target.getId().equals(followerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot follow yourself");
        }
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, target.getId())) {
            return;
        }
        Follow f = new Follow();
        f.setFollowerId(followerId);
        f.setFollowingId(target.getId());
        followRepository.save(f);
    }

    @Transactional
    public void unfollow(int followerId, String targetNick) {
        UserAccount target = userAccountRepository.findByNickIgnoreCase(targetNick)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        followRepository.deleteByFollowerIdAndFollowingId(followerId, target.getId());
    }
}
