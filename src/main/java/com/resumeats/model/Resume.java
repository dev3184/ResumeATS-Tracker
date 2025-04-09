package com.resumeats.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    private int score;

    @Column(length = 5000) // Stores AI-generated suggestions only
    private String suggestions;

    // @ManyToOne
    // @JoinColumn(name = "user_id", nullable = false)
    // private User user;
}
