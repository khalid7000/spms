package com.rit.spms.controller;

import com.rit.spms.domain.EmployeeTitle;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.repository.PortfolioCategoryRepository;
import com.rit.spms.repository.TitleRankLabelRepository;
import com.rit.spms.service.PortfolioCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio/titles")
@RequiredArgsConstructor
public class EmployeeTitleController {

    private final PortfolioCategoryService categoryService;
    private final PortfolioCategoryRepository categoryRepository;
    private final TitleRankLabelRepository rankLabelRepository;

    /**
     * Auto-syncs from every title string actually in use (active users, department heads,
     * org-group heads) before listing, so the dropdown always reflects live org data rather
     * than a manually-curated list.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<EmployeeTitleResponse>>> getAllTitles() {
        List<EmployeeTitleResponse> titles = categoryService.syncAndListAllTitles()
                .stream().map(this::map).toList();
        return ResponseEntity.ok(ApiResponse.success(titles));
    }

    @lombok.Data
    public static class EmployeeTitleResponse {
        private Long id;
        private String titleName;
        private Long departmentId;
        private Boolean isSystemDefault;
        private boolean hasCategories;
        private boolean hasRankLabels;
    }

    private EmployeeTitleResponse map(EmployeeTitle title) {
        EmployeeTitleResponse resp = new EmployeeTitleResponse();
        resp.setId(title.getId());
        resp.setTitleName(title.getTitleName());
        resp.setDepartmentId(title.getDepartment() != null ? title.getDepartment().getId() : null);
        resp.setIsSystemDefault(title.getIsSystemDefault());
        resp.setHasCategories(categoryRepository.existsByTitleId(title.getId()));
        resp.setHasRankLabels(!rankLabelRepository.findByTitleIdOrderByRank(title.getId()).isEmpty());
        return resp;
    }
}
