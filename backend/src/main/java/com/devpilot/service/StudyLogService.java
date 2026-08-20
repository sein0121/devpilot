package com.devpilot.service;

import com.devpilot.domain.Skill;
import com.devpilot.domain.StudyLog;
import com.devpilot.domain.User;
import com.devpilot.dto.request.UpsertStudyLogRequest;
import com.devpilot.dto.response.StudyLogResponse;
import com.devpilot.global.exception.StudyLogNotFoundException;
import com.devpilot.global.exception.UserNotFoundException;
import com.devpilot.repository.SkillRepository;
import com.devpilot.repository.StudyLogRepository;
import com.devpilot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StudyLogService {

    private final UserRepository userRepository;
    private final StudyLogRepository studyLogRepository;
    private final SkillRepository skillRepository;

    @Transactional(readOnly = true)
    public List<StudyLogResponse> getMyLogs(Long userId) {
        User user = getUser(userId);
        return studyLogRepository.findByUserOrderByDateDesc(user).stream()
                .map(StudyLogResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudyLogResponse getLogByDate(Long userId, LocalDate date) {
        User user = getUser(userId);
        StudyLog log = studyLogRepository.findByUserAndDate(user, date)
                .orElseThrow(() -> new StudyLogNotFoundException(date));
        return StudyLogResponse.from(log);
    }

    @Transactional
    public StudyLogResponse upsertLog(Long userId, LocalDate date, UpsertStudyLogRequest request) {
        User user = getUser(userId);
        Set<Skill> skills = resolveSkills(user, request.skillIds());

        StudyLog log = studyLogRepository.findByUserAndDate(user, date)
                .orElseGet(() -> studyLogRepository.save(StudyLog.create(user, date, request.title(), request.content())));

        log.updateContent(request.title(), request.content());
        log.updateSkills(skills);

        return StudyLogResponse.from(log);
    }

    @Transactional
    public void deleteLog(Long userId, LocalDate date) {
        User user = getUser(userId);
        StudyLog log = studyLogRepository.findByUserAndDate(user, date)
                .orElseThrow(() -> new StudyLogNotFoundException(date));
        studyLogRepository.delete(log);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Set<Skill> resolveSkills(User user, List<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return new HashSet<>();
        }
        // 본인 소유 Skill만 걸러서 연결 (남의 Skill을 몰래 연결하는 것 방지)
        return skillIds.stream()
                .map(id -> skillRepository.findByIdAndUser(id, user))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(java.util.stream.Collectors.toSet());
    }
}