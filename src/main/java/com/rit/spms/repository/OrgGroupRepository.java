package com.rit.spms.repository;

import com.rit.spms.domain.OrgGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrgGroupRepository extends JpaRepository<OrgGroup, Long> {
    List<OrgGroup> findByParentIsNull();
    List<OrgGroup> findByParentId(Long parentId);
    List<OrgGroup> findByHeadId(Long headId);

    @Query("SELECT DISTINCT g.headTitle FROM OrgGroup g WHERE g.headTitle IS NOT NULL")
    List<String> findDistinctHeadTitles();
}
