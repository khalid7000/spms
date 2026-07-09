package com.rit.spms.repository;

import com.rit.spms.domain.EmployeeTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeTitleRepository extends JpaRepository<EmployeeTitle, Long> {
    Optional<EmployeeTitle> findByTitleName(String titleName);
    Optional<EmployeeTitle> findByTitleNameIgnoreCase(String titleName);
    List<EmployeeTitle> findByIsSystemDefaultTrue();
    List<EmployeeTitle> findByDepartmentId(Long departmentId);
    List<EmployeeTitle> findAllByOrderByTitleName();
}
