package com.devpilot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoadmapStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private Roadmap roadmap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id") // nullable — Skill이 삭제되면 여기만 끊김
    private Skill skill;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoadmapStepStatus status;

    private LocalDate targetDate;

    @Column(nullable = false)
    private Integer displayOrder;

    public static RoadmapStep create(
            Roadmap roadmap, Skill skill, String title, String description,
            LocalDate targetDate, Integer displayOrder
    ) {
        RoadmapStep step = new RoadmapStep();
        step.roadmap = roadmap;
        step.skill = skill;
        step.title = title;
        step.description = description;
        step.status = RoadmapStepStatus.TODO;
        step.targetDate = targetDate;
        step.displayOrder = displayOrder;
        return step;
    }

    public void update(String title, String description, LocalDate targetDate, Skill skill) {
        this.title = title;
        this.description = description;
        this.targetDate = targetDate;
        this.skill = skill;
    }

    public void changeStatus(RoadmapStepStatus status) {
        this.status = status;
    }

    public void changeOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void detachSkill() {
        this.skill = null;
    }
}