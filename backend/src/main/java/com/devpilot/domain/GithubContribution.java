package com.devpilot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"github_account_id", "contribution_date"}))
public class GithubContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "github_account_id", nullable = false)
    private GithubAccount githubAccount;

    @Column(name = "contribution_date", nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer count;

    public static GithubContribution of(GithubAccount account, LocalDate date, Integer count) {
        GithubContribution contribution = new GithubContribution();
        contribution.githubAccount = account;
        contribution.date = date;
        contribution.count = count;
        return contribution;
    }
}