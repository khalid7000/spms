package com.rit.spms.repository;

import com.rit.spms.domain.ImprovementTask;
import com.rit.spms.domain.enums.VsmTaskState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ImprovementTaskRepository extends JpaRepository<ImprovementTask, Long> {
    List<ImprovementTask> findByKaizenNode_VsmMap_IdAndStateInOrderByCreatedAtDesc(
            Long vsmMapId, Collection<VsmTaskState> states);

    List<ImprovementTask> findByKaizenNode_VsmMap_IdOrderByCreatedAtDesc(Long vsmMapId);

    List<ImprovementTask> findByKaizenNode_VsmMap_Department_IdAndStateInOrderByCreatedAtDesc(
            Long departmentId, Collection<VsmTaskState> states);

    /** Bulk lookup across every map a user can see -- powers VsmAnalyticsService's cross-map task
     *  funnel rollup (Phase 6) in one query instead of one per map. */
    List<ImprovementTask> findByKaizenNode_VsmMap_IdIn(Collection<Long> vsmMapIds);

    /** Tasks this employee currently owns (pulled) -- half of "my tasks" on the central dashboard;
     *  the other half comes from {@link ImprovementTaskAssigneeRepository#findByEmployeeId}. */
    List<ImprovementTask> findByPulledById(Long userId);

    List<ImprovementTask> findByIdIn(Collection<Long> ids);
}
