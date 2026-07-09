package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One academic year belongs to exactly one university-level {@link Strategy} (its cycle) --
 * initiatives/measurements are copied, and Annual Evaluations seeded, only for strategies sharing
 * that university strategy's {@code planningCycle} (itself plus its department strategies for the
 * same era), not indiscriminately for every deployed strategy system-wide.
 */
@Entity
@Table(name = "academic_year")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_strategy_id", nullable = false)
    private Strategy universityStrategy;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean closed = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
