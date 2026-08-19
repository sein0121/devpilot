package com.devpilot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SkillCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private SkillCategory parent; // null이면 최상위 카테고리

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // null이면 시스템 기본 제공, 값 있으면 해당 유저의 커스텀 카테고리

    public static SkillCategory createDefault(String name, SkillCategory parent) {
        SkillCategory category = new SkillCategory();
        category.name = name;
        category.parent = parent;
        return category;
    }

    public static SkillCategory createCustom(String name, SkillCategory parent, User user) {
        SkillCategory category = new SkillCategory();
        category.name = name;
        category.parent = parent;
        category.user = user;
        return category;
    }
}