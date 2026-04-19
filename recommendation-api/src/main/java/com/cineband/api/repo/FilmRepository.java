package com.cineband.api.repo;

import com.cineband.api.domain.Film;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FilmRepository extends JpaRepository<Film, Integer> {

    @Query(value = "SELECT * FROM Filmy ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Film> findRandomFilm();

    @Query(value = "SELECT * FROM Filmy ORDER BY RAND() LIMIT 2", nativeQuery = true)
    List<Film> findTwoRandomDistinctFilms();
}
