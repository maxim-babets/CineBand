package com.cineband.api.service;

import com.cineband.api.domain.UserAccount;
import com.cineband.api.dto.social.ProfileCardDto;
import com.cineband.api.repo.FollowRepository;
import com.cineband.api.repo.PickHistoryRepository;
import com.cineband.api.repo.UserAccountRepository;
import com.cineband.api.repo.WallPostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocialGraphServiceTest {

    @Mock
    private FollowRepository followRepository;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private WallPostRepository wallPostRepository;
    @Mock
    private PickHistoryRepository pickHistoryRepository;

    @InjectMocks
    private SocialGraphService socialGraphService;

    @Test
    void buildProfileCard_selfHasNoFollowButtonState() {
        UserAccount u = new UserAccount();
        u.setId(7);
        u.setNick("alex");
        u.setDisplayName("Alex");
        u.setEmail("a@b.c");
        when(userAccountRepository.findById(7)).thenReturn(Optional.of(u));
        when(followRepository.countByFollowingId(7)).thenReturn(3L);
        when(followRepository.countByFollowerId(7)).thenReturn(5L);
        when(wallPostRepository.countByUserId(7)).thenReturn(2L);
        when(pickHistoryRepository.countByUserId(7)).thenReturn(9L);

        ProfileCardDto card = socialGraphService.buildProfileCard(7, 7);

        assertThat(card.followerCount()).isEqualTo(3);
        assertThat(card.followingCount()).isEqualTo(5);
        assertThat(card.postsCount()).isEqualTo(2);
        assertThat(card.picksCount()).isEqualTo(9);
        assertThat(card.isSelf()).isTrue();
        assertThat(card.isFollowing()).isNull();
    }

    @Test
    void buildProfileCard_viewerSeesFollowState() {
        UserAccount u = new UserAccount();
        u.setId(2);
        u.setNick("bob");
        u.setDisplayName("Bob");
        u.setEmail("b@b.c");
        when(userAccountRepository.findById(2)).thenReturn(Optional.of(u));
        when(followRepository.countByFollowingId(2)).thenReturn(1L);
        when(followRepository.countByFollowerId(2)).thenReturn(4L);
        when(wallPostRepository.countByUserId(anyInt())).thenReturn(0L);
        when(pickHistoryRepository.countByUserId(anyInt())).thenReturn(0L);
        when(followRepository.existsByFollowerIdAndFollowingId(9, 2)).thenReturn(true);

        ProfileCardDto card = socialGraphService.buildProfileCard(2, 9);

        assertThat(card.isSelf()).isFalse();
        assertThat(card.isFollowing()).isTrue();
    }
}
