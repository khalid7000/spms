package com.rit.spms.repository;

import com.rit.spms.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByCode(String code);
    List<Department> findByActiveTrue();
    boolean existsByCode(String code);
    List<Department> findByHeadId(Long headId);
    List<Department> findByOrgGroupId(Long orgGroupId);

    @Query("SELECT DISTINCT d.headTitle FROM Department d WHERE d.headTitle IS NOT NULL")
    List<String> findDistinctHeadTitles();
}
