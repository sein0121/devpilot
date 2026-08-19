package com.devpilot.service.github;

import com.devpilot.dto.github.GithubRepoApiResponse;
import com.devpilot.dto.github.GithubUserApiResponse;
import com.devpilot.global.exception.GithubApiAuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GithubRestApiService {

    private static final int PER_PAGE = 100;
    private static final int MAX_PAGES = 5; // 최대 500개까지 (안전장치)

    private final RestClient githubRestClient;

    public GithubUserApiResponse fetchUserProfile(String username) {
        try {
            return githubRestClient.get()
                    .uri("/users/{username}", username)
                    .retrieve()
                    .body(GithubUserApiResponse.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new GithubApiAuthException();
        }
    }

    public List<GithubRepoApiResponse> fetchRepositories(String username) {
        List<GithubRepoApiResponse> result = new ArrayList<>();

        for (int page = 1; page <= MAX_PAGES; page++) {
            List<GithubRepoApiResponse> pageResult = fetchRepositoryPage(username, page);

            if (pageResult.isEmpty()) {
                break; // 더 이상 데이터 없음 → 마지막 페이지
            }

            result.addAll(pageResult);

            if (pageResult.size() < PER_PAGE) {
                break; // 요청한 개수보다 적게 왔다는 건 이게 마지막 페이지라는 뜻
            }
        }

        return result;
    }

    private List<GithubRepoApiResponse> fetchRepositoryPage(String username, int page) {
        try {
            return githubRestClient.get()
                    .uri("/users/{username}/repos?per_page={perPage}&sort=pushed&page={page}",
                            username, PER_PAGE, page)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GithubRepoApiResponse>>() {});
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new GithubApiAuthException();
        }
    }
}