package com.devpilot.service.github;

import com.devpilot.domain.GithubAccount;
import com.devpilot.domain.GithubContribution;
import com.devpilot.domain.GithubRepository;
import com.devpilot.domain.User;
import com.devpilot.dto.github.GithubContributionResponse;
import com.devpilot.dto.github.GithubRepoApiResponse;
import com.devpilot.dto.github.GithubUserApiResponse;
import com.devpilot.repository.GithubAccountRepository;
import com.devpilot.repository.GithubContributionRepository;
import com.devpilot.repository.GithubRepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GithubSyncWriter {

    private final GithubAccountRepository githubAccountRepository;
    private final GithubRepositoryRepository githubRepositoryRepository;
    private final GithubContributionRepository githubContributionRepository;

    @Transactional
    public void save(
            User user,
            String username,
            GithubUserApiResponse profile,
            List<GithubRepoApiResponse> repos,
            List<GithubContributionResponse.Day> contributions
    ) {
        GithubAccount account = githubAccountRepository.findByUser(user)
                .orElseGet(() -> githubAccountRepository.save(GithubAccount.create(user, username)));

        account.updateProfile(profile.public_repos(), profile.followers(), profile.following());

        syncRepositories(account, repos);
        syncContributions(account, contributions);
    }

    private void syncRepositories(GithubAccount account, List<GithubRepoApiResponse> repos) {
        githubRepositoryRepository.deleteByGithubAccount(account);

        List<GithubRepository> entities = repos.stream()
                .map(r -> GithubRepository.of(
                        account,
                        r.name(),
                        r.description(),
                        r.language(),
                        r.stargazers_count(),
                        r.fork(),
                        LocalDateTime.parse(r.pushed_at(), DateTimeFormatter.ISO_DATE_TIME)
                ))
                .toList();

        githubRepositoryRepository.saveAll(entities);
    }

    private void syncContributions(GithubAccount account, List<GithubContributionResponse.Day> days) {
        LocalDate from = LocalDate.now().minusYears(1);
        LocalDate to = LocalDate.now();

        githubContributionRepository.deleteAllInBatch(
                githubContributionRepository.findByGithubAccountAndDateBetween(account, from, to)
        );

        List<GithubContribution> entities = days.stream()
                .filter(day -> day.contributionCount() != null && day.contributionCount() > 0)
                .map(day -> GithubContribution.of(
                        account,
                        LocalDate.parse(day.date()),
                        day.contributionCount()
                ))
                .toList();

        githubContributionRepository.saveAll(entities);
    }
}