package com.rit.spms.service;

import com.rit.spms.domain.RepositoryRecord;
import com.rit.spms.repository.RepositoryRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Runs a registered {@link RepositoryReader} against an uploaded file and persists the results into
 * {@link RepositoryRecord}. Uses "replace" semantics: for every distinct secondaryKey the run
 * touches, any existing rows for that (sourceType, secondaryKey) are deleted first -- so re-running
 * an import for the same term/type cleanly supersedes the old data instead of accumulating
 * duplicates on every re-upload.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RepositoryImportService {

    private final RepositoryReaderRegistry readerRegistry;
    private final RepositoryRecordRepository recordRepository;

    public ImportResult runImport(String readerCode, MultipartFile file, java.util.Map<String, String> params) throws IOException {
        RepositoryReader reader = readerRegistry.require(readerCode);
        RepositoryReader.ReadResult result = reader.read(file, params);

        Set<String> touchedKeys = result.records().stream()
                .map(RepositoryReader.ParsedRecord::secondaryKey)
                .collect(Collectors.toSet());
        for (String key : touchedKeys) {
            recordRepository.deleteBySourceTypeAndSecondaryKey(readerCode, key);
        }

        List<RepositoryRecord> toSave = result.records().stream()
                .map(r -> RepositoryRecord.builder()
                        .sourceType(readerCode)
                        .secondaryKey(r.secondaryKey())
                        .secondaryKeyLabel(r.secondaryKeyLabel())
                        .employeeEmail(r.employeeEmail())
                        .value(r.value())
                        .build())
                .toList();
        recordRepository.saveAll(toSave);

        return new ImportResult(toSave.size(), result.warnings());
    }

    public record ImportResult(int recordsCreated, List<String> warnings) {}
}
