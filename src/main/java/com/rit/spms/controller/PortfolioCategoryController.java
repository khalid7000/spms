package com.rit.spms.controller;

import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.PortfolioCategory;
import com.rit.spms.domain.TitleRankLabel;
import com.rit.spms.domain.CategoryCriteria;
import com.rit.spms.domain.CriteriaAchievementModule;
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
        private List<String> achievementModuleCodes;
        // Head-only viewer tool(s) assigned here -- a criterion can carry more than one (e.g. both
        // an Early-Alert-flavored and a Grade-Distribution-flavored Central Repository Viewer).
        private List<InfoToolAssignmentSummary> infoToolAssignments;
    }

    @lombok.Data
    public static class InfoToolAssignmentSummary {
        private String toolCode;
        private String displayName;
        private String repositorySourceType;
    }

    @lombok.Data
    public static class AchievementModuleResponse {
        private String code;
        private String displayName;
        private String buttonLabel;
        private String description;
    }

    @lombok.Data
    public static class AchievementModuleAssignmentResponse {
        private String moduleCode;
        private Long criteriaId;
        private String criteriaName;
        private Long categoryId;
        private String categoryName;
        private Integer maxAchievementsPerYear;
        private Boolean mandatory;
        private String displayName;
    }

    @lombok.Data
    public static class AssignAchievementModuleRequest {
        @jakarta.validation.constraints.NotNull private Long criteriaId;
        @jakarta.validation.constraints.NotNull
        @jakarta.validation.constraints.Positive
        private Integer maxAchievementsPerYear;
        private Boolean mandatory;
        private String displayName;
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
        resp.setAchievementModuleCodes(categoryService.getAchievementModulesForCriteria(criteria.getId())
                .stream().map(CriteriaAchievementModule::getModuleCode).toList());
        resp.setInfoToolAssignments(categoryService.getInfoToolsForCriteria(criteria.getId()).stream().map(a -> {
            InfoToolAssignmentSummary summary = new InfoToolAssignmentSummary();
            summary.setToolCode(a.getToolCode());
            summary.setDisplayName(a.getDisplayName());
            summary.setRepositorySourceType(a.getRepositorySourceType());
            return summary;
        }).toList());
        return resp;
    }

    // Customizable Achievement Module assignment

    // Metadata only (code/display name/button label/description) -- every employee's own Annual
    // Evaluation page calls this to label the module buttons on their achievements, so it can't be
    // admin-only like the assignment endpoints below it.
    @GetMapping("/achievement-modules")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AchievementModuleResponse>>> listAchievementModules() {
        List<AchievementModuleResponse> modules = categoryService.listAchievementModules().stream().map(m -> {
            AchievementModuleResponse resp = new AchievementModuleResponse();
            resp.setCode(m.getCode());
            resp.setDisplayName(m.getDisplayName());
            resp.setButtonLabel(m.getButtonLabel());
            resp.setDescription(m.getDescription());
            return resp;
        }).toList();
        return ResponseEntity.ok(ApiResponse.success(modules));
    }

    @GetMapping("/titles/{titleId}/achievement-module-assignments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AchievementModuleAssignmentResponse>>> getAchievementModuleAssignments(
            @PathVariable Long titleId) {
        List<AchievementModuleAssignmentResponse> assignments = categoryService.getAchievementModuleAssignmentsForTitle(titleId)
                .stream().map(a -> {
                    AchievementModuleAssignmentResponse resp = new AchievementModuleAssignmentResponse();
                    resp.setModuleCode(a.getModuleCode());
                    resp.setCriteriaId(a.getCriteria().getId());
                    resp.setCriteriaName(a.getCriteria().getCriteriaName());
                    resp.setCategoryId(a.getCriteria().getCategory().getId());
                    resp.setCategoryName(a.getCriteria().getCategory().getCategoryName());
                    resp.setMaxAchievementsPerYear(a.getMaxAchievementsPerYear());
                    resp.setMandatory(a.getMandatory());
                    resp.setDisplayName(a.getDisplayName());
                    return resp;
                }).toList();
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }

    @PostMapping("/achievement-modules/{code}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignAchievementModule(
            @PathVariable String code, @Valid @RequestBody AssignAchievementModuleRequest req) {
        categoryService.assignAchievementModule(code, req.getCriteriaId(), req.getMaxAchievementsPerYear(), req.getMandatory(), req.getDisplayName());
        return ResponseEntity.ok(ApiResponse.success("Achievement module assigned", null));
    }

    @DeleteMapping("/achievement-modules/{code}/assign/{criteriaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> unassignAchievementModule(
            @PathVariable String code, @PathVariable Long criteriaId) {
        categoryService.unassignAchievementModule(code, criteriaId);
        return ResponseEntity.ok(ApiResponse.success("Achievement module unassigned", null));
    }

    // Criteria Info Tool assignment (head-only viewer, parallel to achievement modules above)

    @lombok.Data
    public static class InfoToolResponse {
        private String code;
        private String description;
    }

    @lombok.Data
    public static class InfoToolAssignmentResponse {
        private String toolCode;
        private Long criteriaId;
        private String criteriaName;
        private Long categoryId;
        private String categoryName;
        private String displayName;
        private String repositorySourceType;
    }

    @lombok.Data
    public static class AssignInfoToolRequest {
        @jakarta.validation.constraints.NotNull private Long criteriaId;
        @jakarta.validation.constraints.NotBlank private String displayName;
        private String repositorySourceType;
    }

    // Same reasoning as GET /achievement-modules -- metadata only, not admin-only, since the
    // assignment UI needs it but so does nothing else right now (heads get displayName/toolCode
    // straight off CriteriaResultResponse instead of calling this).
    @GetMapping("/info-tools")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<InfoToolResponse>>> listInfoTools() {
        List<InfoToolResponse> tools = categoryService.listInfoTools().stream().map(t -> {
            InfoToolResponse resp = new InfoToolResponse();
            resp.setCode(t.getCode());
            resp.setDescription(t.getDescription());
            return resp;
        }).toList();
        return ResponseEntity.ok(ApiResponse.success(tools));
    }

    @GetMapping("/titles/{titleId}/info-tool-assignments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<InfoToolAssignmentResponse>>> getInfoToolAssignments(
            @PathVariable Long titleId) {
        List<InfoToolAssignmentResponse> assignments = categoryService.getInfoToolAssignmentsForTitle(titleId)
                .stream().map(a -> {
                    InfoToolAssignmentResponse resp = new InfoToolAssignmentResponse();
                    resp.setToolCode(a.getToolCode());
                    resp.setCriteriaId(a.getCriteria().getId());
                    resp.setCriteriaName(a.getCriteria().getCriteriaName());
                    resp.setCategoryId(a.getCriteria().getCategory().getId());
                    resp.setCategoryName(a.getCriteria().getCategory().getCategoryName());
                    resp.setDisplayName(a.getDisplayName());
                    resp.setRepositorySourceType(a.getRepositorySourceType());
                    return resp;
                }).toList();
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }

    @PostMapping("/info-tools/{code}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignInfoTool(
            @PathVariable String code, @Valid @RequestBody AssignInfoToolRequest req) {
        categoryService.assignInfoTool(code, req.getCriteriaId(), req.getDisplayName(), req.getRepositorySourceType());
        return ResponseEntity.ok(ApiResponse.success("Info tool assigned", null));
    }

    @DeleteMapping("/info-tools/{code}/assign/{criteriaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> unassignInfoTool(
            @PathVariable String code, @PathVariable Long criteriaId,
            @RequestParam(required = false) String repositorySourceType) {
        categoryService.unassignInfoTool(code, criteriaId, repositorySourceType);
        return ResponseEntity.ok(ApiResponse.success("Info tool unassigned", null));
    }

    @GetMapping("/repository-types")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<String>>> getRepositoryTypes() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getRepositoryTypes()));
    }
}
