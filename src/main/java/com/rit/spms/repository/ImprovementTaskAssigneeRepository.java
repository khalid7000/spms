package com.rit.spms.repository;

import com.rit.spms.domain.ImprovementTaskAssignee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ImprovementTaskAssigneeRepository extends JpaRepository<ImprovementTaskAssignee, Long> {
    List<ImprovementTaskAssignee> findByImprovementTaskId(Long taskId);

    List<ImprovementTaskAssignee> findByImprovementTaskIdIn(Collection<Long> taskIds);

    List<ImprovementTaskAssignee> findByEmployeeId(Long employeeId);

    Optional<ImprovementTaskAssignee> findByImprovementTaskIdAndEmployeeId(Long taskId, Long employeeId);

    void deleteByImprovementTaskId(Long taskId);
}
