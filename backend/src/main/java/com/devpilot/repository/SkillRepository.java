package com.devpilot.repository;

import com.devpilot.domain.Skill;
import com.devpilot.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByUserOrderByDisplayOrderAsc(User user);
    Optional<Skill> findByIdAndUser(Long id, User user); // 본인 소유 확인용
}