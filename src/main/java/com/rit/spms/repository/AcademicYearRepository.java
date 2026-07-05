package com.rit.spms.repository;

import com.rit.spms.domain.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {
    List<AcademicYear> findAllByOrderByCreatedAtDesc();
    boolean existsByName(String name);
    Optional<AcademicYear> findByName(String name);
}
