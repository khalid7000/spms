package com.rit.spms.security;

import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.enums.SystemRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security principal for an authenticated {@link AppUser}. Authorities are derived from
 * {@link SystemRole}s (ROLE_ADMIN/ROLE_HR as applicable, always plus ROLE_USER) -- there's no
 * separate "Employee" role since every authenticated user already has that base capability.
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final Set<SystemRole> systemRoles;
    private final Boolean mustChangePassword;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String email, String passwordHash,
                         Set<SystemRole> systemRoles, Boolean mustChangePassword) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.systemRoles = systemRoles == null ? EnumSet.noneOf(SystemRole.class) : systemRoles;
        this.mustChangePassword = mustChangePassword != null && mustChangePassword;
        this.authorities = buildAuthorities(this.systemRoles);
    }

    private static Collection<? extends GrantedAuthority> buildAuthorities(Set<SystemRole> systemRoles) {
        List<GrantedAuthority> authorities = systemRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toCollection(java.util.ArrayList::new));
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        return authorities;
    }

    public boolean hasRole(SystemRole role) {
        return systemRoles.contains(role);
    }

    public static UserPrincipal from(AppUser user) {
        return new UserPrincipal(
                user.getId(), user.getEmail(), user.getPasswordHash(),
                user.getSystemRoles(), user.getMustChangePassword());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
