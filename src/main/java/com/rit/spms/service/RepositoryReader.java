package com.rit.spms.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A pluggable data-import helper an Admin can run from the Data Repository console to parse a
 * source (a file plus whatever parameters it needs) into rows of the central
 * {@link com.rit.spms.domain.RepositoryRecord} table. Adding a new reader is just a new
 * {@code @Service} implementing this interface -- Spring auto-collects every bean of this type
 * into {@link RepositoryReaderRegistry}, so no call site needs to change (same idiom as
 * {@link CustomizableAchievementModule}). Actual persistence (including "replace prior data for
 * this run" semantics) is centralized in {@link RepositoryImportService}, not here -- a reader's
 * only job is turning a source into {@link ParsedRecord}s.
 */
public interface RepositoryReader {

    /** Stable identifier persisted as repository_record.source_type -- never change once shipped. */
    String getCode();

    /** Shown to the Admin when picking a reader to run. */
    String getDisplayName();

    /** Admin-facing helper text describing what this reader expects and does. */
    String getDescription();

    /** Parses the uploaded file (plus any reader-specific params, e.g. a term) into records ready to store. */
    ReadResult read(MultipartFile file, Map<String, String> params) throws IOException;

    record ParsedRecord(String secondaryKey, String secondaryKeyLabel, String employeeEmail, String value) {}

    /** {@code warnings} covers recoverable per-row issues (e.g. an unmatched name) that didn't stop the import. */
    record ReadResult(List<ParsedRecord> records, List<String> warnings) {}
}
