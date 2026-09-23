package com.devpilot.repository;

import com.devpilot.domain.Skill;
import com.devpilot.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByUserOrderByDisplayOrderAsc(User user);
    Optional<Skill> findByIdAndUser(Long id, User user);

    @Query("""
            SELECT s FROM Skill s
            LEFT JOIN FETCH s.category
            WHERE s.user = :user
            ORDER BY s.displayOrder ASC
            """)
    List<Skill> findByUserWithCategoryOrderByDisplayOrderAsc(@Param("user") User user);
}