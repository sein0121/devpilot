package com.devpilot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "log_date"})) //하루에 1개만 작성
public class StudyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "log_date", nullable = false)
    private LocalDate date;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToMany
    @JoinTable(
            name = "study_log_skill",
            joinColumns = @JoinColumn(name = "study_log_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

    public static StudyLog create(User user, LocalDate date, String content) {
        StudyLog log = new StudyLog();
        log.user = user;
        log.date = date;
        log.content = content;
        return log;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateSkills(Set<Skill> skills) {
        this.skills.clear();
        this.skills.addAll(skills);
    }
}