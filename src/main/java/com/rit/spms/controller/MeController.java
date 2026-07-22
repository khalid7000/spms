package com.rit.spms.controller;

import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.UserResponse;
import com.rit.spms.repository.AppUserRepository;
import com.rit.spms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The module-owned identity endpoint the Academic Dashboard Gateway integration contract
 * requires (ยง3.1, ยง6.4 of the developer guide): identity plus this app's own authorization
 * hints (roles), sourced from StratAlign's own data after the Gateway JWT (or, for every
 * other deployment, this app's own JWT) has already been verified upstream by whichever
 * authentication filter set the SecurityContext. Not gated by gateway-sso.enabled -- it's a
 * plain authenticated endpoint useful regardless of which session type reached it.
 */
@RestController
@RequiredArgsConstructor
public class MeController {

    private final AppUserRepository appUserRepository;

    @GetMapping("/api/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(@AuthenticationPrincipal UserPrincipal principal) {
        return appUserRepository.findByEmail(principal.getEmail())
                .map(user -> ResponseEntity.ok(ApiResponse.success(UserResponse.from(user))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
