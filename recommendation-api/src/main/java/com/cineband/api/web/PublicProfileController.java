package com.cineband.api.web;

import com.cineband.api.domain.Film;
import com.cineband.api.domain.PickHistory;
import com.cineband.api.dto.social.PickDto;
import com.cineband.api.dto.social.ProfileCardDto;
import com.cineband.api.repo.FilmRepository;
import com.cineband.api.repo.PickHistoryRepository;
import com.cineband.api.repo.UserAccountRepository;
import com.cineband.api.service.SocialGraphService;
import com.cineband.api.util.AuthUsers;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class PublicProfileController {

    private final UserAccountRepository userAccountRepository;
    private final PickHistoryRepository pickHistoryRepository;
    private final FilmRepository filmRepository;
    private final SocialGraphService socialGraphService;

    public PublicProfileController(
            UserAccountRepository userAccountRepository,
            PickHistoryRepository pickHistoryRepository,
            FilmRepository filmRepository,
            SocialGraphService socialGraphService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.pickHistoryRepository = pickHistoryRepository;
        this.filmRepository = filmRepository;
        this.socialGraphService = socialGraphService;
    }

    @GetMapping("/by-nick/{nick}/profile")
    public ProfileCardDto profileByNick(@PathVariable String nick, Authentication authentication) {
        return socialGraphService.profileByNick(nick, AuthUsers.currentUserId(authentication));
    }

    @GetMapping("/by-nick/{nick}/picks")
    public List<PickDto> picksByNick(@PathVariable String nick) {
        var user = userAccountRepository.findByNickIgnoreCase(nick)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return pickHistoryRepository.findByUserIdOrderByMomentDesc(user.getId()).stream()
                .map(this::toPickDto)
                .toList();
    }

    private PickDto toPickDto(PickHistory h) {
        Film f = filmRepository.findById(h.getFilmId())
                .orElseThrow(() -> new IllegalStateException("Missing film " + h.getFilmId()));
        return new PickDto(h.getId(), f.getId(), f.getTitle(), h.getMoment());
    }
}
