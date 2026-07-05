package com.rit.spms.controller;

import com.rit.spms.config.LdapProperties;
import com.rit.spms.domain.AppUser;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.exception.BusinessRuleException;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
        data.put("isAdmin", principal.getIsAdmin());
        data.put("mustChangePassword", principal.getMustChangePassword());

        return ResponseEntity.ok(ApiResponse.success("Login successful", data));
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
            throw new BadCredentialsException("Current password is incorrect");
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
        data.put("isAdmin", updated.getIsAdmin());
        data.put("mustChangePassword", updated.getMustChangePassword());

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", data));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@Valid @RequestBody RegisterRequest request) {
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Email already registered");
        }
        var user = adminService.createUser(
                request.getFname(), request.getLname(), request.getEmail(),
                request.getTitle(), null, false, request.getPassword());

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
