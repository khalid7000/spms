package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * The 1-5 rank scale (with admin-customizable wording) is specified once per {@link EmployeeTitle}
 * and applies to every category under that title, as well as the final overall rank given during
 * an evaluation cycle -- rather than being repeated per category.
 */
@Entity
@Table(name = "title_rank_label")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TitleRankLabel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id", nullable = false)
    private EmployeeTitle title;

    @Column(nullable = false)
    private Integer rank;

    @Column(nullable = false, length = 200)
    private String label;

    @Column(columnDefinition = "TEXT")
    private String description;
}
