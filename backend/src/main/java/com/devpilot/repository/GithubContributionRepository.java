package com.devpilot.repository;

import com.devpilot.domain.GithubAccount;
import com.devpilot.domain.GithubContribution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GithubContributionRepository extends JpaRepository<GithubContribution, Long> {
    List<GithubContribution> findByGithubAccountAndDateBetween(
            GithubAccount githubAccount, LocalDate start, LocalDate end
    );
    Optional<GithubContribution> findByGithubAccountAndDate(GithubAccount githubAccount, LocalDate date);
}