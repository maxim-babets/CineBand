package com.cineband.api.repo;

import com.cineband.api.domain.WatchOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WatchOptionRepository extends JpaRepository<WatchOption, Integer> {

    List<WatchOption> findByFilmIdOrderByProviderAsc(Integer filmId);
}
