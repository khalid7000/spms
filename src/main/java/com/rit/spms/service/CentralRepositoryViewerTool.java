package com.rit.spms.service;

import com.rit.spms.domain.CriteriaInfoToolAssignment;
import com.rit.spms.domain.RepositoryRecord;
import com.rit.spms.repository.RepositoryRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * First (and so far only) {@link CriteriaInfoTool}: reads back rows from the central {@link
 * RepositoryRecord} table -- whatever an Admin's chosen {@link RepositoryReader} imported there --
 * filtered to this assignment's {@code repositorySourceType} and the employee being evaluated.
 * Knows nothing about any specific reader's key format; relies entirely on {@code secondaryKeyLabel}
 * for display, which is why every reader is expected to supply one.
 */
@Service
@RequiredArgsConstructor
public class CentralRepositoryViewerTool implements CriteriaInfoTool {

    public static final String CODE = "CENTRAL_REPOSITORY_VIEWER";

    private final RepositoryRecordRepository recordRepository;

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public String getDescription() {
        return "Looks up records from the central data repository for this employee, filtered by "
                + "the type an Admin selects when assigning this tool (e.g. Early Alert) and the "
                + "term(s) the head picks.";
    }

    @Override
    public List<InfoOption> listAvailableOptions(CriteriaInfoToolAssignment assignment, String firstName, String lastName, String email) {
        List<Object[]> rows = recordRepository.findDistinctSecondaryKeysForEmployee(assignment.getRepositorySourceType(), email);
        // findDistinctSecondaryKeysForEmployee can return more than one label per key if data was
        // imported inconsistently -- keep the first label seen per key rather than erroring.
        Map<String, String> keyToLabel = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String key = (String) row[0];
            String label = (String) row[1];
            keyToLabel.putIfAbsent(key, label != null ? label : key);
        }
        return keyToLabel.entrySet().stream()
                .map(e -> new InfoOption(e.getKey(), e.getValue()))
                .toList();
    }

    @Override
    public String getDetails(CriteriaInfoToolAssignment assignment, String firstName, String lastName, String email, List<String> selectedOptionKeys) {
        List<RepositoryRecord> records = recordRepository.findBySourceTypeAndEmployeeEmailAndSecondaryKeyIn(
                assignment.getRepositorySourceType(), email, selectedOptionKeys);
        if (records.isEmpty()) {
            return "No records found for the selected term(s).";
        }
        StringBuilder text = new StringBuilder();
        for (String key : selectedOptionKeys) {
            List<RepositoryRecord> forKey = records.stream().filter(r -> r.getSecondaryKey().equals(key)).toList();
            if (forKey.isEmpty()) {
                continue;
            }
            String label = forKey.get(0).getSecondaryKeyLabel() != null ? forKey.get(0).getSecondaryKeyLabel() : key;
            text.append("=== ").append(label).append(" ===\n\n");
            for (RepositoryRecord record : forKey) {
                text.append(record.getValue()).append("\n");
            }
            text.append("\n");
        }
        return text.toString().trim();
    }
}
