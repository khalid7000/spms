package com.rit.spms.domain.enums;

/**
 * System-wide roles a user can hold in addition to the implicit base "Employee" capability
 * (owning/editing/viewing strategies, having a portfolio) every AppUser already has.
 * A user can hold any combination of these at once.
 */
public enum SystemRole {
    ADMIN,
    HR,
    /** Limited admin: user management only (create/edit users, department assignment, CSV import)
     *  -- cannot reach any other admin console feature and cannot grant ADMIN/HR/USER_ADMIN to
     *  anyone (see AdminService.createUser/updateUser). Only a true ADMIN can grant this role. */
    USER_ADMIN
}
