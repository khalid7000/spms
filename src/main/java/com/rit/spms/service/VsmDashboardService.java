package com.rit.spms.service;

import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.Department;
import com.rit.spms.domain.enums.VsmTaskState;
import com.rit.spms.dto.response.ImprovementTaskResponse;
import com.rit.spms.dto.response.VsmDashboardResponse;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Backs the central "My Tasks" dashboard: what's available to pull in the employee's own
 * department, and everything they already own or collaborate on across every map. Deliberately
 * scoped to the viewer's own department for the "available" side -- a head sees their department's
 * board the same as any member of it; browsing *other* departments' boards is still the existing
 * per-department board pages, not folded in here.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VsmDashboardService {

    private static final int PREVIEW_LIMIT = 5;

    private final AppUserRepository appUserRepository;
    private final VsmBoardService vsmBoardService;
    private final ImprovementTaskService improvementTaskService;

    public VsmDashboardResponse getMyDashboard(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", userId));
        Department department = user.getDepartment();

        List<ImprovementTaskResponse> available = List.of();
        if (department != null) {
            available = vsmBoardService.getDepartmentBoard(department.getId(), userId).stream()
                    .filter(t -> t.getState() == VsmTaskState.AVAILABLE)
                    .toList();
        }

        return VsmDashboardResponse.builder()
                .departmentId(department != null ? department.getId() : null)
                .departmentName(department != null ? department.getName() : null)
                .availableToPullCount(available.size())
                .availableToPullPreview(available.stream().limit(PREVIEW_LIMIT).toList())
                .myTasks(improvementTaskService.getMyTasks(userId))
                .build();
    }
}
