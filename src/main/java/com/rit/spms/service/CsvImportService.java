package com.rit.spms.service;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.Department;
import com.rit.spms.repository.AppUserRepository;
import com.rit.spms.repository.DepartmentRepository;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CsvImportService {

    private final AppUserRepository appUserRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public CsvImportResult importUsers(MultipartFile file) {
        List<UserCsvRow> rows;
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            CsvToBean<UserCsvRow> csvToBean = new CsvToBeanBuilder<UserCsvRow>(reader)
                    .withType(UserCsvRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSkipLines(0)
                    .build();
            rows = csvToBean.parse();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage(), e);
        }

        List<Department> allDepts = departmentRepository.findAll();
        Map<String, Department> deptByCode = allDepts.stream()
                .collect(Collectors.toMap(
                        d -> d.getCode().toLowerCase().trim(),
                        Function.identity(),
                        (a, b) -> a));

        int imported = 0;
        int updated = 0;
        List<CsvImportResult.RowError> errors = new ArrayList<>();
        String defaultHash = passwordEncoder.encode("changeme");

        for (int i = 0; i < rows.size(); i++) {
            UserCsvRow row = rows.get(i);
            int lineNum = i + 2; // 1-based + header

            if (isBlank(row.getEmail())) {
                errors.add(new CsvImportResult.RowError(lineNum, "Email is required"));
                continue;
            }
            if (isBlank(row.getFname()) || isBlank(row.getLname())) {
                errors.add(new CsvImportResult.RowError(lineNum, "First and last name are required"));
                continue;
            }

            Department dept = null;
            if (!isBlank(row.getDepartment())) {
                dept = deptByCode.get(row.getDepartment().toLowerCase().trim());
                if (dept == null) {
                    errors.add(new CsvImportResult.RowError(lineNum,
                            "Department code not found: " + row.getDepartment()));
                    continue;
                }
            }

            String email = row.getEmail().trim().toLowerCase();
            AppUser existing = appUserRepository.findByEmail(email).orElse(null);

            if (existing != null) {
                existing.setFname(row.getFname().trim());
                existing.setLname(row.getLname().trim());
                existing.setTitle(row.getTitle());
                existing.setDepartment(dept);
                appUserRepository.save(existing);
                updated++;
            } else {
                AppUser user = AppUser.builder()
                        .fname(row.getFname().trim())
                        .lname(row.getLname().trim())
                        .email(email)
                        .title(row.getTitle())
                        .department(dept)
                        .isAdmin(false)
                        .active(true)
                        .passwordHash(defaultHash)
                        .build();
                appUserRepository.save(user);
                imported++;
            }
        }

        return new CsvImportResult(imported, updated, errors);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    @Data
    @NoArgsConstructor
    public static class UserCsvRow {
        @CsvBindByName(column = "fname", required = false)
        private String fname;

        @CsvBindByName(column = "lname", required = false)
        private String lname;

        @CsvBindByName(column = "department", required = false)
        private String department;

        @CsvBindByName(column = "email", required = false)
        private String email;

        @CsvBindByName(column = "title", required = false)
        private String title;
    }

    public record CsvImportResult(int imported, int updated, List<RowError> errors) {
        public record RowError(int row, String message) {}
    }
}
