package com.rit.spms.repository;

import com.rit.spms.domain.AchievementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementTypeRepository extends JpaRepository<AchievementType, Long> {
    List<AchievementType> findByActiveTrue();
    boolean existsByName(String name);
}
