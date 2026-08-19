package com.devpilot.service.github;

import com.devpilot.dto.github.GithubRepoApiResponse;
import com.devpilot.dto.github.GithubUserApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.devpilot.global.exception.GithubApiAuthException;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GithubRestApiService {

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
        try {
            return githubRestClient.get()
                    .uri("/users/{username}/repos?per_page=100&sort=pushed", username)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<GithubRepoApiResponse>>() {});
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new GithubApiAuthException();
        }
    }
}