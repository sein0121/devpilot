package com.devpilot.repository;

import com.devpilot.domain.GithubAccount;
import com.devpilot.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GithubAccountRepository extends JpaRepository<GithubAccount, Long> {
    Optional<GithubAccount> findByUser(User user);
    Optional<GithubAccount> findByUserId(Long userId);
}