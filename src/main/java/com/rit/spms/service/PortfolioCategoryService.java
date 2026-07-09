package com.rit.spms.service;

import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.CategoryCriteria;
import com.rit.spms.domain.TitleRankLabel;
import com.rit.spms.domain.EmployeeTitle;
import com.rit.spms.domain.PortfolioCategory;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PortfolioCategoryService {

    private final PortfolioCategoryRepository categoryRepository;
    private final EmployeeTitleRepository titleRepository;
    private final TitleRankLabelRepository rankLabelRepository;
    private final CategoryCriteriaRepository criteriaRepository;
    private final AppUserRepository appUserRepository;
    private final DepartmentRepository departmentRepository;
    private final OrgGroupRepository orgGroupRepository;

    // Category Management

    public PortfolioCategory createCategory(Long titleId, String categoryName, String description) {
        EmployeeTitle title = titleRepository.findById(titleId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeTitle", titleId));

        if (categoryRepository.findByTitleIdAndCategoryName(titleId, categoryName).isPresent()) {
            throw new BusinessRuleException("Category '" + categoryName + "' already exists for this title");
        }

        PortfolioCategory category = PortfolioCategory.builder()
                .title(title)
                .categoryName(categoryName)
                .description(description)
                .build();

        return categoryRepository.save(category);
    }

    public PortfolioCategory updateCategory(Long categoryId, String categoryName, String description) {
        PortfolioCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("PortfolioCategory", categoryId));

        if (!category.getCategoryName().equals(categoryName)) {
            if (categoryRepository.findByTitleIdAndCategoryName(category.getTitle().getId(), categoryName).isPresent()) {
                throw new BusinessRuleException("Category '" + categoryName + "' already exists for this title");
            }
            category.setCategoryName(categoryName);
        }

        category.setDescription(description);
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long categoryId) {
        PortfolioCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("PortfolioCategory", categoryId));

        if (category.getIsSystemDefault()) {
            throw new BusinessRuleException("Cannot delete system default categories");
        }

        categoryRepository.delete(category);
    }

    public PortfolioCategory getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("PortfolioCategory", categoryId));
    }

    public List<PortfolioCategory> getCategoriesByTitle(Long titleId) {
        return categoryRepository.findByTitleIdOrderBySortOrder(titleId);
    }

    /**
     * A user's own {@code AppUser.title} plus the {@code headTitle} of any department they head
     * (e.g. someone who is both "Faculty" and, as department head, "Chair") -- so portfolio
     * categories resolve against every title a user actually holds, not just their primary one.
     */
    private Set<String> getEffectiveTitleNames(AppUser user) {
        Set<String> names = new LinkedHashSet<>();
        if (user.getTitle() != null && !user.getTitle().isBlank()) {
            names.add(user.getTitle().trim());
        }
        for (var dept : departmentRepository.findByHeadId(user.getId())) {
            if (dept.getHeadTitle() != null && !dept.getHeadTitle().isBlank()) {
                names.add(dept.getHeadTitle().trim());
            }
        }
        return names;
    }

    /**
     * Resolves the evaluation categories that apply to a given employee, by matching each of
     * their effective title names (see {@link #getEffectiveTitleNames}) against
     * {@link EmployeeTitle#getTitleName()} (case-insensitive) and unioning the categories under
     * every title that resolves -- deduplicated by category id, in title-then-sort-order.
     * AppUser.title isn't an FK to EmployeeTitle -- titles are typically populated via LDAP as
     * plain strings -- so this is a best-effort lookup rather than a strict join.
     */
    public List<PortfolioCategory> getCategoriesForUser(AppUser user) {
        Set<String> titleNames = getEffectiveTitleNames(user);
        if (titleNames.isEmpty()) {
            throw new BusinessRuleException("This user has no title set; an admin must assign one before categories can be resolved");
        }

        List<PortfolioCategory> categories = new java.util.ArrayList<>();
        Set<Long> seenCategoryIds = new java.util.HashSet<>();
        for (String titleName : titleNames) {
            Optional<EmployeeTitle> title = titleRepository.findByTitleNameIgnoreCase(titleName);
            if (title.isEmpty()) continue;
            for (PortfolioCategory c : categoryRepository.findByTitleIdOrderBySortOrder(title.get().getId())) {
                if (seenCategoryIds.add(c.getId())) categories.add(c);
            }
        }
        if (categories.isEmpty()) {
            throw new BusinessRuleException(
                    "No portfolio categories are configured for title(s) '" + String.join(", ", titleNames) + "' yet -- ask an admin to set them up");
        }
        return categories;
    }

    /**
     * Non-throwing eligibility check, for callers (e.g. bulk auto-seeding) that need to skip an
     * ineligible user rather than fail. Deliberately not just a try/catch around
     * {@link #getCategoriesForUser} at the call site: this is itself a `@Transactional` bean, and
     * an exception crossing that boundary marks the caller's shared transaction rollback-only even
     * if the caller then catches it -- silently poisoning an outer batch operation with an
     * `UnexpectedRollbackException` at commit time instead of the real cause.
     */
    public boolean hasCategoriesForUser(AppUser user) {
        return getEffectiveTitleNames(user).stream().anyMatch(titleName ->
                titleRepository.findByTitleNameIgnoreCase(titleName)
                        .map(title -> categoryRepository.existsByTitleId(title.getId()))
                        .orElse(false));
    }

    /**
     * Titles shown in the admin's Portfolio Category dropdown auto-sync from whatever title
     * strings are actually in use across active users, department heads, and org-group heads --
     * rather than being manually curated -- so nothing needing configuration is ever missing.
     * Any title string not yet backed by an {@link EmployeeTitle} row is created here (empty,
     * with zero categories) so the admin can immediately configure it.
     */
    public List<EmployeeTitle> syncAndListAllTitles() {
        Set<String> inUse = new LinkedHashSet<>();
        inUse.addAll(appUserRepository.findDistinctActiveUserTitles());
        inUse.addAll(departmentRepository.findDistinctHeadTitles());
        inUse.addAll(orgGroupRepository.findDistinctHeadTitles());

        Set<String> existingLower = titleRepository.findAll().stream()
                .map(t -> t.getTitleName().toLowerCase())
                .collect(Collectors.toSet());

        for (String titleName : inUse) {
            String trimmed = titleName == null ? null : titleName.trim();
            if (trimmed == null || trimmed.isEmpty()) continue;
            if (existingLower.contains(trimmed.toLowerCase())) continue;
            titleRepository.save(EmployeeTitle.builder().titleName(trimmed).build());
            existingLower.add(trimmed.toLowerCase());
        }

        return titleRepository.findAllByOrderByTitleName();
    }

    // Rank Label Management (per Title -- applies to every category under it, and the final
    // overall rank given during an evaluation cycle)

    public TitleRankLabel addRankLabel(Long titleId, Integer rank, String label, String description) {
        EmployeeTitle title = titleRepository.findById(titleId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeTitle", titleId));

        if (rank < 1 || rank > 5) {
            throw new BusinessRuleException("Rank must be between 1 and 5");
        }

        if (rankLabelRepository.findByTitleIdAndRank(titleId, rank).isPresent()) {
            throw new BusinessRuleException("Rank label for rank " + rank + " already exists");
        }

        TitleRankLabel rankLabel = TitleRankLabel.builder()
                .title(title)
                .rank(rank)
                .label(label)
                .description(description)
                .build();

        return rankLabelRepository.save(rankLabel);
    }

    public TitleRankLabel updateRankLabel(Long rankLabelId, String label, String description) {
        TitleRankLabel rankLabel = rankLabelRepository.findById(rankLabelId)
                .orElseThrow(() -> new ResourceNotFoundException("TitleRankLabel", rankLabelId));

        rankLabel.setLabel(label);
        rankLabel.setDescription(description);
        return rankLabelRepository.save(rankLabel);
    }

    public void deleteRankLabel(Long rankLabelId) {
        TitleRankLabel rankLabel = rankLabelRepository.findById(rankLabelId)
                .orElseThrow(() -> new ResourceNotFoundException("TitleRankLabel", rankLabelId));
        rankLabelRepository.delete(rankLabel);
    }

    public List<TitleRankLabel> getRankLabelsByTitle(Long titleId) {
        return rankLabelRepository.findByTitleIdOrderByRank(titleId);
    }

    // Criteria Management

    public CategoryCriteria addCriteria(Long categoryId, String criteriaName, String description,
                                         String rubricUnsatisfactory, String rubricMeetsExpectations, String rubricExceedsExpectations) {
        PortfolioCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("PortfolioCategory", categoryId));

        int maxSortOrder = criteriaRepository.findByCategoryIdOrderBySortOrder(categoryId)
                .stream().mapToInt(CategoryCriteria::getSortOrder).max().orElse(0);

        CategoryCriteria criteria = CategoryCriteria.builder()
                .category(category)
                .criteriaName(criteriaName)
                .description(description)
                .sortOrder(maxSortOrder + 1)
                .rubricUnsatisfactory(rubricUnsatisfactory)
                .rubricMeetsExpectations(rubricMeetsExpectations)
                .rubricExceedsExpectations(rubricExceedsExpectations)
                .build();

        return criteriaRepository.save(criteria);
    }

    public CategoryCriteria updateCriteria(Long criteriaId, String criteriaName, String description,
                                            String rubricUnsatisfactory, String rubricMeetsExpectations, String rubricExceedsExpectations) {
        CategoryCriteria criteria = criteriaRepository.findById(criteriaId)
                .orElseThrow(() -> new ResourceNotFoundException("CategoryCriteria", criteriaId));

        criteria.setCriteriaName(criteriaName);
        criteria.setDescription(description);
        criteria.setRubricUnsatisfactory(rubricUnsatisfactory);
        criteria.setRubricMeetsExpectations(rubricMeetsExpectations);
        criteria.setRubricExceedsExpectations(rubricExceedsExpectations);
        return criteriaRepository.save(criteria);
    }

    public void deleteCriteria(Long criteriaId) {
        CategoryCriteria criteria = criteriaRepository.findById(criteriaId)
                .orElseThrow(() -> new ResourceNotFoundException("CategoryCriteria", criteriaId));
        criteriaRepository.delete(criteria);
    }

    public List<CategoryCriteria> getCriteriaByCategory(Long categoryId) {
        return criteriaRepository.findByCategoryIdOrderBySortOrder(categoryId);
    }

    public void reorderCriteria(Long categoryId, List<Long> criteriaIds) {
        for (int i = 0; i < criteriaIds.size(); i++) {
            Long criteriaId = criteriaIds.get(i);
            CategoryCriteria criteria = criteriaRepository.findById(criteriaId)
                    .orElseThrow(() -> new ResourceNotFoundException("CategoryCriteria", criteriaId));
            criteria.setSortOrder(i);
            criteriaRepository.save(criteria);
        }
    }
}
