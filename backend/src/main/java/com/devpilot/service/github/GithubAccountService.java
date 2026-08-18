package com.devpilot.service.github;

import com.devpilot.domain.GithubAccount;
import com.devpilot.domain.GithubContribution;
import com.devpilot.domain.GithubRepository;
import com.devpilot.domain.User;
import com.devpilot.dto.github.GithubContributionResponse;
import com.devpilot.dto.github.GithubRepoApiResponse;
import com.devpilot.dto.github.GithubUserApiResponse;
import com.devpilot.dto.response.GithubAccountResponse;
import com.devpilot.global.exception.UserNotFoundException;
import com.devpilot.repository.GithubAccountRepository;
import com.devpilot.repository.GithubContributionRepository;
import com.devpilot.repository.GithubRepositoryRepository;
import com.devpilot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GithubAccountService {

    private final UserRepository userRepository;
    private final GithubAccountRepository githubAccountRepository; 
    private final GithubRepositoryRepository githubRepositoryRepository; 
    private final GithubContributionRepository githubContributionRepository;
    private final GithubRestApiService githubRestApiService;
    private final GithubGraphQlApiService githubGraphQlApiService;
    private final GithubSyncWriter githubSyncWriter; // 저장 전용 클래스를 주입받아 위임

    public void syncGithubData(Long userId) {
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

        String username = user.getNickname();

        // 1) 외부 API 호출 (트랜잭션 없음)
        GithubUserApiResponse profile = githubRestApiService.fetchUserProfile(username);
        List<GithubRepoApiResponse> repos = githubRestApiService.fetchRepositories(username);

        LocalDate to = LocalDate.now();
        LocalDate from = to.minusYears(1);
        List<GithubContributionResponse.Day> contributions =
                githubGraphQlApiService.fetchContributions(username, from, to);

        // 2) 저장은 다른 클래스(빈)에 위임 → 프록시를 제대로 거쳐서 @Transactional 정상 동작
        githubSyncWriter.save(user, username, profile, repos, contributions);
    }

    @Transactional(readOnly = true)
    public GithubAccountResponse getMyGithubData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        GithubAccount account = githubAccountRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "아직 동기화된 GitHub 데이터가 없습니다."
                ));

        List<GithubRepository> repos =
                githubRepositoryRepository.findByGithubAccountOrderByPushedAtDesc(account);

        LocalDate from = LocalDate.now().minusYears(1);
        LocalDate to = LocalDate.now();
        List<GithubContribution> contributions =
                githubContributionRepository.findByGithubAccountAndDateBetween(account, from, to);

        return GithubAccountResponse.of(account, repos, contributions);
    }
}