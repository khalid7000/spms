package com.rit.spms.repository;

import com.rit.spms.domain.AnnualEvaluation;
import com.rit.spms.domain.enums.AnnualEvaluationState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnnualEvaluationRepository extends JpaRepository<AnnualEvaluation, Long> {
    Optional<AnnualEvaluation> findByEmployeeIdAndAcademicYearId(Long employeeId, Long academicYearId);
    List<AnnualEvaluation> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
    List<AnnualEvaluation> findByHeadIdAndAcademicYearIdOrderByCreatedAtDesc(Long headId, Long academicYearId);
    boolean existsByEmployeeIdAndAcademicYearId(Long employeeId, Long academicYearId);
    List<AnnualEvaluation> findByAcademicYearIdAndStateOrderByEmployeeId(Long academicYearId, AnnualEvaluationState state);
    List<AnnualEvaluation> findByEmployee_Department_IdInAndAcademicYearId(Collection<Long> departmentIds, Long academicYearId);
}
