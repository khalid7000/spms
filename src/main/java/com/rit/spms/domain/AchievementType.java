package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "achievement_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false)
    private Boolean active = true;

    /** Stable identifier for the "Other" and "Course Evaluation" rows other code branches on --
     *  never settable through the achievement-type admin API, only ever assigned by migration. */
    @Column(name = "system_code", unique = true, length = 50)
    private String systemCode;
}
