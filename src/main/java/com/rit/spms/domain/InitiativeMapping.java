package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "initiative_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiativeMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_initiative_id", nullable = false, unique = true)
    private Initiative deptInitiative;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_initiative_id", nullable = false)
    private Initiative universityInitiative;
}
