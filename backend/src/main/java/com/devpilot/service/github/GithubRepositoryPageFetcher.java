package com.devpilot.service.github;

import com.devpilot.dto.github.GithubRepoApiResponse;
import com.devpilot.global.exception.GithubApiAuthException;
import com.devpilot.global.exception.GithubApiClientException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
class GithubRepositoryPageFetcher {

    private static final int PER_PAGE = 100;

    private final RestClient githubRestClient;

    @Retry(name = "github")
    @CircuitBreaker(name = "github")
    List<GithubRepoApiResponse> fetchPage(String username, int page) {
        try {
            return githubRestClient.get()
                    .uri("/users/{username}/repos?per_page={perPage}&sort=pushed&page={page}",
                            username, PER_PAGE, page)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GithubRepoApiResponse>>() {});
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new GithubApiAuthException();
        } catch (HttpClientErrorException e) {
            throw new GithubApiClientException("GitHub API 요청이 거부되었습니다: " + e.getStatusCode());
        }
    }
}