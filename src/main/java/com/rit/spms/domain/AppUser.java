package com.rit.spms.domain;

import com.rit.spms.domain.enums.SystemRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * A system user. Every user is implicitly an "Employee" (can own/edit/view strategies, has a
 * portfolio) -- that base capability isn't stored here. {@link #systemRoles} layers on top of it:
 * ADMIN and/or HR, any combination, controlling console visibility and elevated permissions.
 */
@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fname;

    @Column(nullable = false, length = 100)
    private String lname;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Column(length = 200)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_group_id")
    private OrgGroup orgGroup;

    @ElementCollection(targetClass = SystemRole.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "app_user_system_role", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private Set<SystemRole> systemRoles = new HashSet<>();

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "must_change_password", nullable = false)
    @Builder.Default
    private Boolean mustChangePassword = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public boolean hasRole(SystemRole role) {
        return systemRoles != null && systemRoles.contains(role);
    }
}
