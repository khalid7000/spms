package com.rit.spms.platform.controller;

import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.platform.domain.PlatformAdmin;
import com.rit.spms.platform.repository.PlatformAdminRepository;
import com.rit.spms.platform.security.PlatformJwtTokenProvider;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/** Login for Super Admins -- entirely separate from {@code AuthController}, which is for
 * tenant {@code AppUser}s. See {@link PlatformJwtTokenProvider}'s javadoc for why. */
@RestController
@RequestMapping("/api/platform/auth")
@RequiredArgsConstructor
public class PlatformAuthController {

    private final PlatformAdminRepository platformAdminRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlatformJwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginRequest request) {
        PlatformAdmin admin = platformAdminRepository.findByEmail(request.getEmail())
                .filter(PlatformAdmin::getActive)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = tokenProvider.generateToken(admin.getId(), admin.getEmail());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("email", admin.getEmail());

        return ResponseEntity.ok(ApiResponse.success("Login successful", data));
    }

    @Data
    public static class LoginRequest {
        @NotBlank
        private String email;
        @NotBlank
        private String password;
    }
}
