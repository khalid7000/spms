package com.rit.spms.controller;

import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.PortfolioCategory;
import com.rit.spms.domain.TitleRankLabel;
import com.rit.spms.domain.CategoryCriteria;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.AppUserRepository;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.PortfolioCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioCategoryController {

    private final PortfolioCategoryService categoryService;
    private final AppUserRepository appUserRepository;

    // My categories (resolved from the current user's title)

    @GetMapping("/my-categories")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getMyCategories(
            @AuthenticationPrincipal UserPrincipal principal) {
        AppUser user = appUserRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", principal.getId()));
        List<CategoryResponse> categories = categoryService.getCategoriesForUser(user)
                .stream().map(this::map).toList();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /** Categories for an arbitrary employee -- used when a strategy Owner edits an achievement authored by someone else. */
    @GetMapping("/categories-for-employee/{employeeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoriesForEmployee(@PathVariable Long employeeId) {
        AppUser employee = appUserRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", employeeId));
        List<CategoryResponse> categories = categoryService.getCategoriesForUser(employee)
                .stream().map(this::map).toList();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    // Category Management

    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest req) {
        PortfolioCategory category = categoryService.createCategory(req.getTitleId(), req.getCategoryName(), req.getDescription());
        return ResponseEntity.status(201).body(ApiResponse.success("Category created", map(category)));
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(@PathVariable Long id) {
        PortfolioCategory category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(map(category)));
    }

    @GetMapping("/titles/{titleId}/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoriesByTitle(@PathVariable Long titleId) {
        List<CategoryResponse> categories = categoryService.getCategoriesByTitle(titleId)
                .stream().map(this::map).toList();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest req) {
        PortfolioCategory category = categoryService.updateCategory(id, req.getCategoryName(), req.getDescription());
        return ResponseEntity.ok(ApiResponse.success("Category updated", map(category)));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }

    // Rank Label Management (per Title -- applies to every category under it)

    @PostMapping("/titles/{titleId}/rank-labels")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RankLabelResponse>> addRankLabel(
            @PathVariable Long titleId,
            @Valid @RequestBody AddRankLabelRequest req) {
        TitleRankLabel label = categoryService.addRankLabel(titleId, req.getRank(), req.getLabel(), req.getDescription());
        return ResponseEntity.status(201).body(ApiResponse.success("Rank label added", map(label)));
    }

    @GetMapping("/titles/{titleId}/rank-labels")
    public ResponseEntity<ApiResponse<List<RankLabelResponse>>> getRankLabels(@PathVariable Long titleId) {
        List<RankLabelResponse> labels = categoryService.getRankLabelsByTitle(titleId)
                .stream().map(this::map).toList();
        return ResponseEntity.ok(ApiResponse.success(labels));
    }

    @PutMapping("/rank-labels/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RankLabelResponse>> updateRankLabel(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRankLabelRequest req) {
        TitleRankLabel label = categoryService.updateRankLabel(id, req.getLabel(), req.getDescription());
        return ResponseEntity.ok(ApiResponse.success("Rank label updated", map(label)));
    }

    @DeleteMapping("/rank-labels/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRankLabel(@PathVariable Long id) {
        categoryService.deleteRankLabel(id);
        return ResponseEntity.ok(ApiResponse.success("Rank label deleted", null));
    }

    // Criteria Management

    @PostMapping("/categories/{categoryId}/criteria")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CriteriaResponse>> addCriteria(
            @PathVariable Long categoryId,
            @Valid @RequestBody AddCriteriaRequest req) {
        CategoryCriteria criteria = categoryService.addCriteria(categoryId, req.getCriteriaName(), req.getDescription(),
                req.getRubricUnsatisfactory(), req.getRubricMeetsExpectations(), req.getRubricExceedsExpectations());
        return ResponseEntity.status(201).body(ApiResponse.success("Criteria added", map(criteria)));
    }

    @GetMapping("/categories/{categoryId}/criteria")
    public ResponseEntity<ApiResponse<List<CriteriaResponse>>> getCriteria(@PathVariable Long categoryId) {
        List<CriteriaResponse> criteria = categoryService.getCriteriaByCategory(categoryId)
                .stream().map(this::map).toList();
        return ResponseEntity.ok(ApiResponse.success(criteria));
    }

    @PutMapping("/criteria/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CriteriaResponse>> updateCriteria(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCriteriaRequest req) {
        CategoryCriteria criteria = categoryService.updateCriteria(id, req.getCriteriaName(), req.getDescription(),
                req.getRubricUnsatisfactory(), req.getRubricMeetsExpectations(), req.getRubricExceedsExpectations());
        return ResponseEntity.ok(ApiResponse.success("Criteria updated", map(criteria)));
    }

    @DeleteMapping("/criteria/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCriteria(@PathVariable Long id) {
        categoryService.deleteCriteria(id);
        return ResponseEntity.ok(ApiResponse.success("Criteria deleted", null));
    }

    @PutMapping("/categories/{categoryId}/criteria/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> reorderCriteria(
            @PathVariable Long categoryId,
            @RequestBody List<Long> criteriaIds) {
        categoryService.reorderCriteria(categoryId, criteriaIds);
        return ResponseEntity.ok(ApiResponse.success("Criteria reordered", null));
    }

    // DTOs

    @lombok.Data
    public static class CategoryResponse {
        private Long id;
        private Long titleId;
        private String categoryName;
        private String description;
        private Integer sortOrder;
        private List<CriteriaResponse> criteria;
    }

    @lombok.Data
    public static class RankLabelResponse {
        private Long id;
        private Integer rank;
        private String label;
        private String description;
    }

    @lombok.Data
    public static class CriteriaResponse {
        private Long id;
        private String criteriaName;
        private String description;
        private Integer sortOrder;
        private String rubricUnsatisfactory;
        private String rubricMeetsExpectations;
        private String rubricExceedsExpectations;
    }

    @lombok.Data
    public static class CreateCategoryRequest {
        private Long titleId;
        private String categoryName;
        private String description;
    }

    @lombok.Data
    public static class UpdateCategoryRequest {
        private String categoryName;
        private String description;
    }

    @lombok.Data
    public static class AddRankLabelRequest {
        private Integer rank;
        private String label;
        private String description;
    }

    @lombok.Data
    public static class UpdateRankLabelRequest {
        private String label;
        private String description;
    }

    @lombok.Data
    public static class AddCriteriaRequest {
        private String criteriaName;
        private String description;
        private String rubricUnsatisfactory;
        private String rubricMeetsExpectations;
        private String rubricExceedsExpectations;
    }

    @lombok.Data
    public static class UpdateCriteriaRequest {
        private String criteriaName;
        private String description;
        private String rubricUnsatisfactory;
        private String rubricMeetsExpectations;
        private String rubricExceedsExpectations;
    }

    // Mapping helpers

    private CategoryResponse map(PortfolioCategory category) {
        CategoryResponse resp = new CategoryResponse();
        resp.setId(category.getId());
        resp.setTitleId(category.getTitle().getId());
        resp.setCategoryName(category.getCategoryName());
        resp.setDescription(category.getDescription());
        resp.setSortOrder(category.getSortOrder());
        resp.setCriteria(category.getCriteria().stream().map(this::map).toList());
        return resp;
    }

    private RankLabelResponse map(TitleRankLabel label) {
        RankLabelResponse resp = new RankLabelResponse();
        resp.setId(label.getId());
        resp.setRank(label.getRank());
        resp.setLabel(label.getLabel());
        resp.setDescription(label.getDescription());
        return resp;
    }

    private CriteriaResponse map(CategoryCriteria criteria) {
        CriteriaResponse resp = new CriteriaResponse();
        resp.setId(criteria.getId());
        resp.setCriteriaName(criteria.getCriteriaName());
        resp.setDescription(criteria.getDescription());
        resp.setSortOrder(criteria.getSortOrder());
        resp.setRubricUnsatisfactory(criteria.getRubricUnsatisfactory());
        resp.setRubricMeetsExpectations(criteria.getRubricMeetsExpectations());
        resp.setRubricExceedsExpectations(criteria.getRubricExceedsExpectations());
        return resp;
    }
}
