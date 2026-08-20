package com.devpilot.repository;

import com.devpilot.domain.StudyLog;
import com.devpilot.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudyLogRepository extends JpaRepository<StudyLog, Long> {
    Optional<StudyLog> findByUserAndDate(User user, LocalDate date);
    List<StudyLog> findByUserOrderByDateDesc(User user);
}