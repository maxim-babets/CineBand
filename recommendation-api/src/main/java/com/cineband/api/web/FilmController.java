package com.cineband.api.web;

import com.cineband.api.domain.Film;
import com.cineband.api.dto.MovieDetailResponse;
import com.cineband.api.repo.FilmRepository;
import com.cineband.api.service.MovieDetailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class FilmController {

    private final FilmRepository filmRepository;
    private final MovieDetailService movieDetailService;

    public FilmController(FilmRepository filmRepository, MovieDetailService movieDetailService) {
        this.filmRepository = filmRepository;
        this.movieDetailService = movieDetailService;
    }

    /** Full catalog for browse. Surprise duel uses {@code GET /api/surprise-pair} instead. */
    @GetMapping
    public List<Film> list() {
        return filmRepository.findAll();
    }

    /**
     * Random title for “Surprise me” — includes where-to-watch options when seeded.
     */
    @GetMapping("/random")
    public MovieDetailResponse random() {
        return movieDetailService.getRandomDetail();
    }

    @GetMapping("/{id:\\d+}")
    public MovieDetailResponse byId(@PathVariable int id) {
        return movieDetailService.getDetail(id);
    }
}
