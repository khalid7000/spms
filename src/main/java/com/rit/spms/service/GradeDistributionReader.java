package com.rit.spms.service;

import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Second repository reader: parses a "Grade Dist Courses" Excel export (one row per course
 * section, one column per letter-grade percentage) for a given term. Same shape as {@link
 * EarlyAlertReader} -- Admin supplies year/term, the instructor name column resolves the employee,
 * every other column becomes a text line -- except the workbook has a single unnamed sheet (no
 * multi-tab/naming convention to validate) and the instructor name lives in column D, not K.
 */
@Service
@RequiredArgsConstructor
public class GradeDistributionReader implements RepositoryReader {

    public static final String CODE = "GRADE_DISTRIBUTION";

    // Column D (0-indexed 3) holds "Instructor Name" in this export, e.g. "Francis,Dali".
    private static final int INSTRUCTOR_NAME_COLUMN = 3;

    private final AppUserRepository appUserRepository;

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public String getDisplayName() {
        return "Grade Distribution";
    }

    @Override
    public String getDescription() {
        return "Imports a Grade Distribution Excel export for a given term -- one record per course "
                + "section row, matched to the instructor by name and filed under that instructor's email.";
    }

    @Override
    public ReadResult read(MultipartFile file, Map<String, String> params) throws IOException {
        String year = params == null ? null : params.get("year");
        String term = params == null ? null : params.get("term");
        if (year == null || year.isBlank() || !year.matches("\\d{4}")) {
            throw new BusinessRuleException("A 4-digit year is required");
        }
        if (term == null || term.isBlank()) {
            throw new BusinessRuleException("A term (Fall/Spring/Summer) is required");
        }
        String termCode = TermCodeUtil.computeTermCode(year, term);
        String termLabel = TermCodeUtil.computeTermLabel(year, term);

        List<String> warnings = new ArrayList<>();
        List<ParsedRecord> records;

        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            if (workbook.getNumberOfSheets() < 1) {
                throw new BusinessRuleException("The workbook has no sheets");
            }
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, String> nameToEmail = InstructorNameMatcher.buildNameToEmailIndex(appUserRepository.findByActiveTrue());
            records = NameColumnSheetParser.parse(sheet, INSTRUCTOR_NAME_COLUMN, termCode, termLabel, nameToEmail, warnings);
        }

        return new ReadResult(records, warnings);
    }
}
