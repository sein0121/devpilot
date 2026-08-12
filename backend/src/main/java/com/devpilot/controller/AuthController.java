package com.devpilot.controller;

import com.devpilot.dto.response.UserResponse;
import com.devpilot.global.response.ApiResponse;
import com.devpilot.global.security.DevPilotOAuth2User;
import com.devpilot.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false); // 세션 없으면 새로 만들지 않음
        if (session != null) {
            session.invalidate(); // 서버 측 세션 제거
        }

        Cookie cookie = new Cookie("JSESSIONID", "");
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료 → 브라우저가 쿠키 삭제
        response.addCookie(cookie);

        return ApiResponse.success(null);
    }
}