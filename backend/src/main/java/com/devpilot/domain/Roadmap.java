package com.devpilot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    private LocalDate targetDate;

    public static Roadmap create(User user, String title, String description, LocalDate targetDate) {
        Roadmap roadmap = new Roadmap();
        roadmap.user = user;
        roadmap.title = title;
        roadmap.description = description;
        roadmap.targetDate = targetDate;
        return roadmap;
    }

    public void update(String title, String description, LocalDate targetDate) {
        this.title = title;
        this.description = description;
        this.targetDate = targetDate;
    }
}