package com.devpilot.dto.response;

import com.devpilot.domain.StudyLog;

import java.time.LocalDate;
import java.util.List;

public record StudyLogResponse(
        Long id,
        LocalDate date,
        String content,
        List<String> skillNames
) {
    public static StudyLogResponse from(StudyLog log) {
        return new StudyLogResponse(
                log.getId(),
                log.getDate(),
                log.getContent(),
                log.getSkills().stream().map(s -> s.getName()).toList()
        );
    }
}