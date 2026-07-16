package com.rit.spms.service;

import com.rit.spms.service.RepositoryReader.ParsedRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared "one Excel sheet, one instructor-name column, every other column becomes a text line"
 * parsing logic behind every {@link RepositoryReader} of this shape (first: {@link
 * EarlyAlertReader}, {@link GradeDistributionReader}). Header row = row 0, data starts row 1; the
 * name column is excluded from each record's own text since it's only used to resolve the
 * employee. One sheet row = one {@link ParsedRecord}; rows whose name doesn't resolve to a known
 * employee are skipped and reported as a warning rather than failing the whole import.
 */
final class NameColumnSheetParser {

    private NameColumnSheetParser() {
    }

    static List<ParsedRecord> parse(Sheet sheet, int nameColumn, String secondaryKey, String secondaryKeyLabel,
                                      Map<String, String> nameToEmail, List<String> warnings) {
        List<ParsedRecord> records = new ArrayList<>();
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new com.rit.spms.exception.BusinessRuleException("The data sheet has no header row");
        }
        DataFormatter formatter = new DataFormatter();
        Map<Integer, String> headers = new LinkedHashMap<>();
        for (int col = 0; col < headerRow.getLastCellNum(); col++) {
            if (col == nameColumn) {
                continue;
            }
            Cell cell = headerRow.getCell(col);
            String header = cell == null ? "" : formatter.formatCellValue(cell).trim();
            if (!header.isBlank()) {
                headers.put(col, header);
            }
        }

        int lastRow = sheet.getLastRowNum();
        for (int rowIdx = 1; rowIdx <= lastRow; rowIdx++) {
            Row row = sheet.getRow(rowIdx);
            if (row == null) {
                continue;
            }
            Cell nameCell = row.getCell(nameColumn);
            String rawName = nameCell == null ? "" : formatter.formatCellValue(nameCell).trim();
            if (rawName.isBlank()) {
                continue;
            }
            String email = InstructorNameMatcher.resolveEmail(nameToEmail, rawName);
            if (email == null) {
                warnings.add("No matching employee for '" + rawName + "' (row " + (rowIdx + 1) + ")");
                continue;
            }

            StringBuilder value = new StringBuilder();
            for (Map.Entry<Integer, String> h : headers.entrySet()) {
                Cell cell = row.getCell(h.getKey());
                String cellValue = cell == null ? "" : formatter.formatCellValue(cell).trim();
                if (!cellValue.isBlank()) {
                    value.append(h.getValue()).append(": ").append(cellValue).append("\n");
                }
            }
            if (value.length() > 0) {
                records.add(new ParsedRecord(secondaryKey, secondaryKeyLabel, email, value.toString()));
            }
        }
        return records;
    }
}
