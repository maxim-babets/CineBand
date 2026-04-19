package com.cineband.api.repo;

import com.cineband.api.domain.PickHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PickHistoryRepository extends JpaRepository<PickHistory, Integer> {

    List<PickHistory> findByUserIdOrderByMomentDesc(Integer userId);

    long countByUserId(Integer userId);
}
