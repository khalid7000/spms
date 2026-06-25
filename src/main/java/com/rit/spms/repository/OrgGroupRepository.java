package com.rit.spms.repository;

import com.rit.spms.domain.OrgGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrgGroupRepository extends JpaRepository<OrgGroup, Long> {
    List<OrgGroup> findByParentIsNull();
    List<OrgGroup> findByParentId(Long parentId);
}
