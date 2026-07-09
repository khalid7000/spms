package com.rit.spms.dto.response;

import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.Department;
import com.rit.spms.domain.enums.SystemRole;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Set;

/** AppUser projection returned by the admin user-management endpoints, including {@link SystemRole}s (not the boolean isAdmin flag it replaced). */
@Value
@Builder
public class UserResponse {

    Long id;
    String fname;
    String lname;
    String email;
    String title;
    DeptInfo department;
    Set<SystemRole> systemRoles;
    Boolean active;
    Boolean mustChangePassword;
    LocalDateTime createdAt;

    public static UserResponse from(AppUser user) {
        Department dept = user.getDepartment();
        return UserResponse.builder()
                .id(user.getId())
                .fname(user.getFname())
                .lname(user.getLname())
                .email(user.getEmail())
                .title(user.getTitle())
                .department(dept == null ? null : new DeptInfo(dept.getId(), dept.getName(), dept.getCode()))
                .systemRoles(user.getSystemRoles())
                .active(user.getActive())
                .mustChangePassword(user.getMustChangePassword())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public record DeptInfo(Long id, String name, String code) {}
}
