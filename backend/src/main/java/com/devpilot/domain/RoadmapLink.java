package com.devpilot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoadmapLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private Roadmap roadmap;

    @Column(nullable = false, length = 500)
    private String url;

    private String label; // nullable — "공식 문서", "강의" 등

    public static RoadmapLink create(Roadmap roadmap, String url, String label) {
        RoadmapLink link = new RoadmapLink();
        link.roadmap = roadmap;
        link.url = url;
        link.label = label;
        return link;
    }
}