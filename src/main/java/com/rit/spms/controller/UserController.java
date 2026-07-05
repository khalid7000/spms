package com.rit.spms.controller;

import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.UserResponse;
import com.rit.spms.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AppUserRepository appUserRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(
            @RequestParam(defaultValue = "") String q) {
        List<UserResponse> results = appUserRepository
                .searchActive(q, PageRequest.of(0, 30))
                .stream().map(UserResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
