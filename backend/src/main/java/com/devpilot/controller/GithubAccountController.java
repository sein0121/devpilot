package com.devpilot.controller;

import com.devpilot.dto.response.GithubAccountResponse;
import com.devpilot.global.response.ApiResponse;
import com.devpilot.global.security.DevPilotOAuth2User;
import com.devpilot.service.github.GithubAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GithubAccountController {

    private final GithubAccountService githubAccountService;

    @PostMapping("/sync")
    public ApiResponse<Void> sync(@AuthenticationPrincipal DevPilotOAuth2User principal) {
        githubAccountService.syncGithubData(principal.getUserId());
        return ApiResponse.success(null);
    }

    @GetMapping("/me")
    public ApiResponse<GithubAccountResponse> getMe(@AuthenticationPrincipal DevPilotOAuth2User principal) {
        return ApiResponse.success(githubAccountService.getMyGithubData(principal.getUserId()));
    }
}