package com.devpilot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubRepository {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "github_account_id", nullable = false)
    private GithubAccount githubAccount;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private String language;
    private Integer stars;
    private Boolean isFork;
    private LocalDateTime pushedAt;

    public static GithubRepository of(
            GithubAccount account, String name, String description,
            String language, Integer stars, Boolean isFork, LocalDateTime pushedAt
    ) {
        GithubRepository repo = new GithubRepository();
        repo.githubAccount = account;
        repo.name = name;
        repo.description = description;
        repo.language = language;
        repo.stars = stars;
        repo.isFork = isFork;
        repo.pushedAt = pushedAt;
        return repo;
    }
}