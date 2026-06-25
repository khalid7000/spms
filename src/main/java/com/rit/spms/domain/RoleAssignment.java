package com.rit.spms.domain;

import com.rit.spms.domain.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_assignment",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "strategy_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strategy_id", nullable = false)
    private Strategy strategy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleType role;
}
