// package com.devpilot.domain;

// import jakarta.persistence.*;
// import lombok.AccessLevel;
// import lombok.Getter;
// import lombok.NoArgsConstructor;

// import java.time.LocalDate;
// import java.time.LocalDateTime;

// @Entity
// @Getter
// @NoArgsConstructor(access = AccessLevel.PROTECTED)
// public class StudyFeedback {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @ManyToOne(fetch = FetchType.LAZY)
//     @JoinColumn(name = "user_id", nullable = false)
//     private User user;

//     @Column(nullable = false)
//     private LocalDate periodFrom;

//     @Column(nullable = false)
//     private LocalDate periodTo;

//     @Column(nullable = false, columnDefinition = "TEXT")
//     private String content;

//     @Column(nullable = false)
//     private LocalDateTime createdAt;

//     public static StudyFeedback create(User user, LocalDate periodFrom, LocalDate periodTo, String content) {
//         StudyFeedback feedback = new StudyFeedback();
//         feedback.user = user;
//         feedback.periodFrom = periodFrom;
//         feedback.periodTo = periodTo;
//         feedback.content = content;
//         feedback.createdAt = LocalDateTime.now();
//         return feedback;
//     }
// }