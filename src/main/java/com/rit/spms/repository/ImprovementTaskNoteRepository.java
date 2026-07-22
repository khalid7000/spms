package com.rit.spms.repository;

import com.rit.spms.domain.ImprovementTaskNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ImprovementTaskNoteRepository extends JpaRepository<ImprovementTaskNote, Long> {
    List<ImprovementTaskNote> findByImprovementTaskIdOrderByCreatedAtAsc(Long taskId);

    List<ImprovementTaskNote> findByImprovementTaskIdIn(Collection<Long> taskIds);

    long countByImprovementTaskId(Long taskId);
}
