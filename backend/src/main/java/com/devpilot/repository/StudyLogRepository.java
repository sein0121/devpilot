package com.devpilot.repository;

import com.devpilot.domain.StudyLog;
import com.devpilot.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudyLogRepository extends JpaRepository<StudyLog, Long> {
    Optional<StudyLog> findByUserAndDate(User user, LocalDate date);
    List<StudyLog> findByUserOrderByDateDesc(User user);

    @Query("""
            SELECT DISTINCT sl FROM StudyLog sl
            LEFT JOIN FETCH sl.skills
            WHERE sl.user = :user AND sl.date BETWEEN :start AND :end
            ORDER BY sl.date DESC
            """)
    List<StudyLog> findByUserAndDateBetweenWithSkills(
            @Param("user") User user, @Param("start") LocalDate start, @Param("end") LocalDate end
    );
}