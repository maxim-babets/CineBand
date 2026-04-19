package com.cineband.api.repo;

import com.cineband.api.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    Optional<UserAccount> findByNickIgnoreCase(String nick);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByNickIgnoreCase(String nick);
}
