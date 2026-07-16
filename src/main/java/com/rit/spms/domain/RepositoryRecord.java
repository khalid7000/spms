package com.rit.spms.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * One record in the central data repository, written by a {@link com.rit.spms.service.RepositoryReader}
 * and read back by a {@link com.rit.spms.service.CriteriaInfoTool} (currently only
 * {@code CentralRepositoryViewerTool}). Keyed by (sourceType, secondaryKey, employeeEmail) -- that
 * key is intentionally NOT unique, since e.g. one EarlyAlert import can produce several rows for the
 * same instructor/term (one per flagged course). {@code employeeEmail} is a plain string, not an
 * AppUser FK, since imported data may reference people not currently active (or even present) in SPMS.
 */
@Entity
@Table(name = "repository_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Which reader produced this row, e.g. "EARLY_ALERT" -- the first key. */
    @Column(name = "source_type", nullable = false, length = 100)
    private String sourceType;

    /** Reader-defined second key, e.g. an EarlyAlert term code like "2251". */
    @Column(name = "secondary_key", nullable = false, length = 100)
    private String secondaryKey;

    /** Optional human-readable label for secondaryKey (e.g. "Fall 2025" for "2251") -- lets a
     *  generic viewer display something meaningful without knowing the reader's own key format. */
    @Column(name = "secondary_key_label", length = 200)
    private String secondaryKeyLabel;

    /** The employee this record is about -- the third key. */
    @Column(name = "employee_email", nullable = false, length = 200)
    private String employeeEmail;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String value;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
