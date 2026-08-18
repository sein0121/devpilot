package com.devpilot.repository;

import com.devpilot.domain.GithubAccount;
import com.devpilot.domain.GithubRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GithubRepositoryRepository extends JpaRepository<GithubRepository, Long> {
    List<GithubRepository> findByGithubAccountOrderByPushedAtDesc(GithubAccount githubAccount);
    void deleteByGithubAccount(GithubAccount githubAccount);
}