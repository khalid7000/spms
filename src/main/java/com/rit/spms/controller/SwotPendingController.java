package com.rit.spms.controller;

import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.SwotPendingActionResponse;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.SwotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Cross-strategy "things awaiting my action" feed for the SWOT workflow, mirroring the Approvals sidebar badge. */
@RestController
@RequestMapping("/api/swot")
@RequiredArgsConstructor
public class SwotPendingController {

    private final SwotService swotService;

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<SwotPendingActionResponse>>> getPendingActions(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(swotService.getPendingActions(principal.getId())));
    }
}
