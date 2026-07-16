package com.rit.spms.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rit.spms.domain.AnnualEvaluation;
import com.rit.spms.domain.RatingAssistantSelection;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.repository.AnnualEvaluationRepository;
import com.rit.spms.repository.RatingAssistantSelectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Persists a head's Rating Assistant word selections (see RatingAssistantModal) so they're
 * available any time -- not just for the current browser session -- while staying strictly
 * private to the specific head who made them. Every read/write here asserts the caller IS this
 * evaluation's head; there is no exception for the employee, other heads, or admin.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RatingAssistantSelectionService {

    private final RatingAssistantSelectionRepository selectionRepository;
    private final AnnualEvaluationRepository evaluationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public List<String> getSelectionHistory(Long evaluationId, String targetType, Long targetId, Long currentUserId) {
        assertIsHead(evaluationId, currentUserId);
        return selectionRepository.findByEvaluationIdAndHeadIdAndTargetTypeAndTargetId(evaluationId, currentUserId, targetType, targetId)
                .map(s -> parseHistory(s.getSelectionHistory()))
                .orElse(List.of());
    }

    public void saveSelectionHistory(Long evaluationId, String targetType, Long targetId, List<String> selectionHistory, Long currentUserId) {
        AnnualEvaluation evaluation = assertIsHead(evaluationId, currentUserId);
        RatingAssistantSelection selection = selectionRepository
                .findByEvaluationIdAndHeadIdAndTargetTypeAndTargetId(evaluationId, currentUserId, targetType, targetId)
                .orElseGet(() -> RatingAssistantSelection.builder()
                        .evaluation(evaluation)
                        .head(evaluation.getHead())
                        .targetType(targetType)
                        .targetId(targetId)
                        .selectionHistory("[]")
                        .build());
        selection.setSelectionHistory(writeHistory(selectionHistory));
        selectionRepository.save(selection);
    }

    private AnnualEvaluation assertIsHead(Long evaluationId, Long currentUserId) {
        AnnualEvaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("AnnualEvaluation", evaluationId));
        if (!evaluation.getHead().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Rating Assistant selections are private to this evaluation's head");
        }
        return evaluation;
    }

    private List<String> parseHistory(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private String writeHistory(List<String> history) {
        try {
            return objectMapper.writeValueAsString(history != null ? history : List.of());
        } catch (Exception e) {
            throw new BusinessRuleException("Could not save selection: " + e.getMessage());
        }
    }
}
