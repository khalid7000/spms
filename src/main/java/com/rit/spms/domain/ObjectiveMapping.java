package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "objective_mapping",
        uniqueConstraints = @UniqueConstraint(columnNames = {"dept_objective_id", "university_objective_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjectiveMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_objective_id", nullable = false)
    private Objective deptObjective;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_objective_id", nullable = false)
    private Objective universityObjective;
}
