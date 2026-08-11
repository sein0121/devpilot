package com.devpilot.controller;

import com.devpilot.dto.response.UserResponse;
import com.devpilot.global.response.ApiResponse;
import com.devpilot.global.security.DevPilotOAuth2User;
import com.devpilot.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal DevPilotOAuth2User principal) {
        return ApiResponse.success(authService.getCurrentUser(principal));
    }
}