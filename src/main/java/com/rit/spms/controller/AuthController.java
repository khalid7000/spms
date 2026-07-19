package com.rit.spms.controller;

import com.rit.spms.config.LdapProperties;
import com.rit.spms.domain.AppUser;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.AppUserRepository;
import com.rit.spms.security.JwtTokenProvider;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.AdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import java.util.HashMap;
import java.util.Map;

/** Login, self-registration, and password change -- issues/reissues the JWT and reports the user's systemRoles back to the client. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AdminService adminService;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final LdapProperties ldapProperties;
    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        String token = jwtTokenProvider.generateToken(principal);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", principal.getId());
        data.put("email", principal.getEmail());
        data.put("systemRoles", principal.getSystemRoles());
        data.put("mustChangePassword", principal.getMustChangePassword());
        // Lets the frontend hide/guard self-service password change when LDAP owns the
        // password -- see ChangePasswordPage.jsx and MemberLayout.jsx's user menu.
        data.put("ldapEnabled", ldapProperties.isEnabled());

        return ResponseEntity.ok(ApiResponse.success("Login successful", data));
    }

    /** Login for any organization other than the default/root one. The actual schema
     * resolution happens earlier, in {@code TenantResolutionFilter} (registered ahead of
     * {@code JwtAuthenticationFilter} in {@code SecurityConfig}) -- it has to run before
     * Spring's Open-Session-In-View filter opens this request's one shared Hibernate session,
     * which fixes its tenant identifier once, at creation, before any controller code runs.
     * Setting TenantContext here would be too late (verified against a real run: the
     * authentication query still landed on the default schema). This method only re-validates
     * the org exists/is active for a clean 404/409 rather than a confusing auth failure, then
     * delegates to the identical authenticate+token flow above. */
    @PostMapping("/{slug}/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> loginToOrganization(
            @PathVariable String slug, @Valid @RequestBody LoginRequest request) {
        Map<String, Object> row;
        try {
            row = jdbcTemplate.queryForMap(
                    "SELECT status FROM platform.organization WHERE slug = ?", slug);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("Organization not found: " + slug);
        }
        if (!"ACTIVE".equals(row.get("status"))) {
            throw new BusinessRuleException("This organization is not currently active.");
        }

        return login(request);
    }

    @PatchMapping("/change-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {

        if (ldapProperties.isEnabled()) {
            throw new BusinessRuleException(
                    "Password changes are managed by the LDAP directory. " +
                    "Please use your organisation's password reset portal.");
        }

        AppUser user = appUserRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new BusinessRuleException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            // Not an auth failure (the user already holds a valid bearer token) -- a 401 here
            // would trip axios's global interceptor and force-log-out the user for a typo.
            throw new BusinessRuleException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        appUserRepository.save(user);

        // Issue a fresh token so the client's stored state reflects mustChangePassword=false
        UserPrincipal updated = UserPrincipal.from(user);
        String newToken = jwtTokenProvider.generateToken(updated);

        Map<String, Object> data = new HashMap<>();
        data.put("token", newToken);
        data.put("userId", updated.getId());
        data.put("email", updated.getEmail());
        data.put("systemRoles", updated.getSystemRoles());
        data.put("mustChangePassword", updated.getMustChangePassword());
        data.put("ldapEnabled", ldapProperties.isEnabled());

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", data));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@Valid @RequestBody RegisterRequest request) {
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Email already registered");
        }
        var user = adminService.createUser(
                request.getFname(), request.getLname(), request.getEmail(),
                request.getTitle(), null, null, Set.of(), request.getPassword(), null);

        return ResponseEntity.status(201).body(ApiResponse.success("User registered", Map.of(
                "userId", user.getId(),
                "email", user.getEmail()
        )));
    }

    @Data
    public static class LoginRequest {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank
        private String currentPassword;
        @NotBlank @Size(min = 8, message = "New password must be at least 8 characters")
        private String newPassword;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank
        private String fname;
        @NotBlank
        private String lname;
        @NotBlank @Email
        private String email;
        private String title;
        @NotBlank
        private String password;
    }
}
