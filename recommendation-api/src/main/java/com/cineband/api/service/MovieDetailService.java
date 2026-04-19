package com.cineband.api.service;

import com.cineband.api.domain.Film;
import com.cineband.api.dto.MovieDetailResponse;
import com.cineband.api.dto.WatchOptionDto;
import com.cineband.api.repo.FilmRepository;
import com.cineband.api.repo.WatchOptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MovieDetailService {

    private final FilmRepository filmRepository;
    private final WatchOptionRepository watchOptionRepository;

    public MovieDetailService(FilmRepository filmRepository, WatchOptionRepository watchOptionRepository) {
        this.filmRepository = filmRepository;
        this.watchOptionRepository = watchOptionRepository;
    }

    public MovieDetailResponse getDetail(int filmId) {
        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Film not found"));
        return toDetail(film);
    }

    public MovieDetailResponse getRandomDetail() {
        Film film = filmRepository.findRandomFilm()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No films in catalog"));
        return toDetail(film);
    }

    private MovieDetailResponse toDetail(Film film) {
        List<WatchOptionDto> options = watchOptionRepository.findByFilmIdOrderByProviderAsc(film.getId()).stream()
                .map(w -> new WatchOptionDto(w.getProvider(), w.getUrl()))
                .toList();
        return new MovieDetailResponse(
                film.getId(),
                film.getTitle(),
                film.getGenre(),
                film.getReleaseYear(),
                film.getBannerUrl(),
                options
        );
    }

    public List<MovieDetailResponse> getSurpriseDuo() {
        List<Film> two = filmRepository.findTwoRandomDistinctFilms();
        if (two.size() < 2) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Need at least two rows in the Filmy table. Import or seed your movie catalog, then try again."
            );
        }
        return two.stream().map(this::toDetail).toList();
    }
}
