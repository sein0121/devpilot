package com.devpilot.controller;

import com.devpilot.dto.response.CareerAnalysisResponse;
import com.devpilot.global.response.ApiResponse;
import com.devpilot.global.security.DevPilotOAuth2User;
import com.devpilot.service.CareerAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/career-analysis")
@RequiredArgsConstructor
public class CareerAnalysisController {

    private final CareerAnalysisService careerAnalysisService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<CareerAnalysisResponse> trigger(
            @AuthenticationPrincipal DevPilotOAuth2User principal,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return ApiResponse.success(
                careerAnalysisService.triggerAndAccept(principal.getUserId(), idempotencyKey)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<CareerAnalysisResponse> get(
            @AuthenticationPrincipal DevPilotOAuth2User principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(careerAnalysisService.getOwned(principal.getUserId(), id));
    }
}