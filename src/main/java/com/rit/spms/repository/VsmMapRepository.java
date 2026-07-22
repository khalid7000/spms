package com.rit.spms.repository;

import com.rit.spms.domain.VsmMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface VsmMapRepository extends JpaRepository<VsmMap, Long> {
    List<VsmMap> findByDepartmentIdIn(Collection<Long> departmentIds);
    List<VsmMap> findByOrgGroupIdIn(Collection<Long> orgGroupIds);
    List<VsmMap> findByDepartmentId(Long departmentId);
}
