package com.devpilot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GithubAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String githubUsername;

    @Column(length = 500)
    private String avatarUrl; // 깃허브 프로필 이미지 URL

    private Integer publicRepoCount;
    private Integer followerCount;
    private Integer followingCount;

    private LocalDateTime lastSyncedAt;

    public static GithubAccount create(User user, String githubUsername) {
        GithubAccount account = new GithubAccount();
        account.user = user;
        account.githubUsername = githubUsername;
        return account;
    }

    public void updateProfile(Integer publicRepoCount, Integer followerCount, Integer followingCount, String avatarUrl) {
        this.publicRepoCount = publicRepoCount;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.avatarUrl = avatarUrl;
        this.lastSyncedAt = LocalDateTime.now();
    }
}