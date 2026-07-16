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
 * First repository reader: parses the "End of Term Academic Alert Summary" Excel workbook the
 * Advising Office's EarlyAlert system exports. Each workbook covers one term (Admin specifies which
 * via the {@code year}/{@code term} params) and has exactly 3 sheets -- Overview, Column Details,
 * and a data sheet named "{termCode} DUBAI" -- of which only the 3rd is read. One Excel row =
 * one {@link ParsedRecord}, keyed by the resolved instructor's email and this run's term code.
 */
@Service
@RequiredArgsConstructor
public class EarlyAlertReader implements RepositoryReader {

    public static final String CODE = "EARLY_ALERT";

    // Column K (0-indexed 10) holds the instructor's name -- used only to resolve which employee a
    // row belongs to, then excluded from the record's own text (every OTHER column becomes a line).
    private static final int INSTRUCTOR_NAME_COLUMN = 10;
    private static final int DATA_SHEET_INDEX = 2;

    private final AppUserRepository appUserRepository;

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public String getDisplayName() {
        return "Early Alert";
    }

    @Override
    public String getDescription() {
        return "Imports the Advising Office's End of Term Academic Alert Summary Excel export for a "
                + "given term -- one record per flagged course row, matched to the instructor by name "
                + "and filed under that instructor's email.";
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
            if (workbook.getNumberOfSheets() < 3) {
                throw new BusinessRuleException("Expected at least 3 sheets (Overview, Column Details, data) -- found "
                        + workbook.getNumberOfSheets());
            }
            Sheet sheet = workbook.getSheetAt(DATA_SHEET_INDEX);
            String expectedSheetName = termCode + " DUBAI";
            if (!expectedSheetName.equalsIgnoreCase(sheet.getSheetName().trim())) {
                warnings.add("Expected the 3rd sheet to be named \"" + expectedSheetName
                        + "\" but found \"" + sheet.getSheetName() + "\" -- continuing anyway");
            }

            Map<String, String> nameToEmail = InstructorNameMatcher.buildNameToEmailIndex(appUserRepository.findByActiveTrue());
            records = NameColumnSheetParser.parse(sheet, INSTRUCTOR_NAME_COLUMN, termCode, termLabel, nameToEmail, warnings);
        }

        return new ReadResult(records, warnings);
    }
}
