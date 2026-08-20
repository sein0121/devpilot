package com.devpilot.controller;

import com.devpilot.dto.request.UpsertStudyLogRequest;
import com.devpilot.dto.response.StudyLogResponse;
import com.devpilot.global.response.ApiResponse;
import com.devpilot.global.security.DevPilotOAuth2User;
import com.devpilot.service.StudyLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/study-logs")
@RequiredArgsConstructor
public class StudyLogController {

    private final StudyLogService studyLogService;

    @GetMapping
    public ApiResponse<List<StudyLogResponse>> getMyLogs(@AuthenticationPrincipal DevPilotOAuth2User principal) {
        return ApiResponse.success(studyLogService.getMyLogs(principal.getUserId()));
    }

    @GetMapping("/{date}")
    public ApiResponse<StudyLogResponse> getLogByDate(
            @AuthenticationPrincipal DevPilotOAuth2User principal,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(studyLogService.getLogByDate(principal.getUserId(), date));
    }

    @PutMapping("/{date}")
    public ApiResponse<StudyLogResponse> upsertLog(
            @AuthenticationPrincipal DevPilotOAuth2User principal,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody UpsertStudyLogRequest request
    ) {
        return ApiResponse.success(studyLogService.upsertLog(principal.getUserId(), date, request));
    }

    @DeleteMapping("/{date}")
    public ApiResponse<Void> deleteLog(
            @AuthenticationPrincipal DevPilotOAuth2User principal,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        studyLogService.deleteLog(principal.getUserId(), date);
        return ApiResponse.success(null);
    }
}