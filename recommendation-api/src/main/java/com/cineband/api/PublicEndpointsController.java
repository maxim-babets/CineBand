package com.cineband.api;

import com.cineband.api.auth.JwtService;
import com.cineband.api.auth.UserPrincipal;
import com.cineband.api.domain.UserAccount;
import com.cineband.api.dto.MovieDetailResponse;
import com.cineband.api.dto.auth.AuthResponse;
import com.cineband.api.dto.auth.LoginRequest;
import com.cineband.api.dto.auth.RegisterRequest;
import com.cineband.api.dto.auth.UserDto;
import com.cineband.api.repo.UserAccountRepository;
import com.cineband.api.service.MovieDetailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Public demo endpoints colocated with {@link RecommendationApiApplication} so routes are always
 * discovered (same base package as {@code @SpringBootApplication}).
 */
@RestController
public class PublicEndpointsController {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MovieDetailService movieDetailService;

    public PublicEndpointsController(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            MovieDetailService movieDetailService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.movieDetailService = movieDetailService;
    }

    @GetMapping("/api/surprise-pair")
    public List<MovieDetailResponse> surprisePair() {
        return movieDetailService.getSurpriseDuo();
    }

    @PostMapping({"/api/auth/register", "/api/register"})
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        if (userAccountRepository.existsByEmailIgnoreCase(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        if (userAccountRepository.existsByNickIgnoreCase(req.nick())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Handle already taken");
        }
        UserAccount u = new UserAccount();
        u.setDisplayName(req.displayName().trim());
        u.setNick(req.nick().trim());
        u.setEmail(req.email().trim().toLowerCase());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        userAccountRepository.save(u);

        UserPrincipal principal = UserPrincipal.from(u);
        String token = jwtService.generateToken(principal);
        return new AuthResponse(token, "Bearer", toDto(u));
    }

    @PostMapping({"/api/auth/login", "/api/login"})
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email().trim().toLowerCase(), req.password())
        );
        UserAccount u = userAccountRepository.findByEmailIgnoreCase(req.email().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        UserPrincipal principal = UserPrincipal.from(u);
        return new AuthResponse(jwtService.generateToken(principal), "Bearer", toDto(u));
    }

    private static UserDto toDto(UserAccount u) {
        return new UserDto(
                u.getId(),
                u.getEmail(),
                u.getNick() != null ? u.getNick() : "",
                u.getDisplayName()
        );
    }
}
