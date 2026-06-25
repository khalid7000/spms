package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "strategy_approval")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StrategyApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strategy_id", nullable = false)
    private Strategy strategy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "required_approver_id", nullable = false)
    private AppUser requiredApprover;

    /** Display label, e.g. "Chair, Department of Computer Science" */
    @Column(name = "approver_title", nullable = false, length = 300)
    private String approverTitle;

    /** 1 = department head (closest to owner), higher = further up the hierarchy */
    @Column(name = "approval_order", nullable = false)
    private Integer approvalOrder;

    @Builder.Default
    @Column(nullable = false)
    private Boolean approved = false;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
