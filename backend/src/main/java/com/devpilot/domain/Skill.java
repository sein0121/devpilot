package com.devpilot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name; // "Spring", "Kafka" 등

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private SkillCategory category; // nullable — 카테고리 미지정도 허용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillStatus status;

    @Column(nullable = false)
    private Integer proficiency; // 1~5

    @Column(nullable = false)
    private Integer displayOrder;

    public static Skill create(
            User user, String name, SkillCategory category,
            SkillStatus status, Integer proficiency, Integer displayOrder
    ) {
        Skill skill = new Skill();
        skill.user = user;
        skill.name = name;
        skill.category = category;
        skill.status = status;
        skill.proficiency = proficiency;
        skill.displayOrder = displayOrder;
        return skill;
    }

    public void update(String name, SkillCategory category, SkillStatus status, Integer proficiency) {
        this.name = name;
        this.category = category;
        this.status = status;
        this.proficiency = proficiency;
    }

    public void changeOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}