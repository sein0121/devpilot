package com.devpilot.dto.response;

import com.devpilot.domain.GithubAccount;
import com.devpilot.domain.GithubContribution;
import com.devpilot.domain.GithubRepository;

import java.time.LocalDateTime;
import java.util.List;

public record GithubAccountResponse(
        String githubUsername,
        Integer publicRepoCount,
        Integer followerCount,
        Integer followingCount,
        LocalDateTime lastSyncedAt,
        List<RepositoryItem> repositories,
        List<ContributionItem> contributions
) {
    public static GithubAccountResponse of(
            GithubAccount account,
            List<GithubRepository> repos,
            List<GithubContribution> contributions
    ) {
        return new GithubAccountResponse(
                account.getGithubUsername(),
                account.getPublicRepoCount(),
                account.getFollowerCount(),
                account.getFollowingCount(),
                account.getLastSyncedAt(),
                repos.stream().map(RepositoryItem::from).toList(),
                contributions.stream().map(ContributionItem::from).toList()
        );
    }

    public record RepositoryItem(
            String name, String description, String language,
            Integer stars, Boolean isFork
    ) {
        public static RepositoryItem from(GithubRepository repo) {
            return new RepositoryItem(
                    repo.getName(), repo.getDescription(), repo.getLanguage(),
                    repo.getStars(), repo.getIsFork()
            );
        }
    }

    public record ContributionItem(String date, Integer count) {
        public static ContributionItem from(GithubContribution contribution) {
            return new ContributionItem(contribution.getDate().toString(), contribution.getCount());
        }
    }
}