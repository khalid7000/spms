package com.rit.spms.service;

import com.rit.spms.domain.*;
import com.rit.spms.domain.enums.StrategyState;
import com.rit.spms.domain.enums.StrategyType;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExcelExportService {

    private final StrategyRepository strategyRepository;
    private final GoalRepository goalRepository;
    private final ObjectiveRepository objectiveRepository;
    private final InitiativeRepository initiativeRepository;
    private final MeasurementRepository measurementRepository;
    private final AchievementRepository achievementRepository;
    private final ObjectiveMappingRepository objectiveMappingRepository;
    private final InitiativeMappingRepository initiativeMappingRepository;
    private final PermissionService permissionService;

    public byte[] exportStrategy(Long strategyId, Long currentUserId) {
        permissionService.assertOwner(currentUserId, strategyId);

        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            CellStyle headerStyle = createHeaderStyle(wb);

            createOverviewSheet(wb, strategy, headerStyle);
            createGoalsSheet(wb, strategy, headerStyle);
            createObjectivesSheet(wb, strategy, headerStyle);
            createInitiativesSheet(wb, strategy, headerStyle);
            createMeasurementsSheet(wb, strategy, headerStyle);

            if (strategy.getState() == StrategyState.DEPLOYED || strategy.getState() == StrategyState.FROZEN) {
                createAchievementsSheet(wb, strategy, headerStyle);
            }

            wb.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel: " + e.getMessage(), e);
        }
    }

    private void createOverviewSheet(Workbook wb, Strategy strategy, CellStyle headerStyle) {
        Sheet sheet = wb.createSheet("Overview");
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 12000);

        String[][] data = {
                {"Strategy Title", strategy.getTitle()},
                {"Type", strategy.getStrategyType().name()},
                {"State", strategy.getState().name()},
                {"Planning Cycle", strategy.getPlanningCycle().getName()},
                {"Department", strategy.getDepartment() != null ? strategy.getDepartment().getName() : "N/A"},
                {"Description", strategy.getDescription() != null ? strategy.getDescription() : ""},
                {"Cycle Start Year", String.valueOf(strategy.getPlanningCycle().getStartYear())},
                {"Cycle End Year", String.valueOf(strategy.getPlanningCycle().getEndYear())},
        };

        Row hdr = sheet.createRow(0);
        createCell(hdr, 0, "Field", headerStyle);
        createCell(hdr, 1, "Value", headerStyle);

        for (int i = 0; i < data.length; i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(data[i][0]);
            row.createCell(1).setCellValue(data[i][1]);
        }
    }

    private void createGoalsSheet(Workbook wb, Strategy strategy, CellStyle headerStyle) {
        Sheet sheet = wb.createSheet("Goals");
        Row hdr = sheet.createRow(0);
        createCell(hdr, 0, "Goal ID", headerStyle);
        createCell(hdr, 1, "Theme", headerStyle);
        createCell(hdr, 2, "Title", headerStyle);
        createCell(hdr, 3, "Description", headerStyle);
        createCell(hdr, 4, "Sort Order", headerStyle);

        AtomicInteger rowNum = new AtomicInteger(1);
        goalRepository.findByStrategyIdOrderBySortOrder(strategy.getId()).forEach(goal -> {
            Row row = sheet.createRow(rowNum.getAndIncrement());
            row.createCell(0).setCellValue(goal.getId());
            row.createCell(1).setCellValue(goal.getTheme() != null ? goal.getTheme().getName() : "");
            row.createCell(2).setCellValue(goal.getTitle());
            row.createCell(3).setCellValue(goal.getDescription() != null ? goal.getDescription() : "");
            row.createCell(4).setCellValue(goal.getSortOrder());
        });

        autoSizeColumns(sheet, 5);
    }

    private void createObjectivesSheet(Workbook wb, Strategy strategy, CellStyle headerStyle) {
        Sheet sheet = wb.createSheet("Objectives");
        Row hdr = sheet.createRow(0);
        createCell(hdr, 0, "Objective ID", headerStyle);
        createCell(hdr, 1, "Goal ID", headerStyle);
        createCell(hdr, 2, "Title", headerStyle);
        createCell(hdr, 3, "Description", headerStyle);
        createCell(hdr, 4, "Frozen", headerStyle);
        createCell(hdr, 5, "University Objective IDs (if dept)", headerStyle);

        AtomicInteger rowNum = new AtomicInteger(1);
        List<Goal> goals = goalRepository.findByStrategyIdOrderBySortOrder(strategy.getId());
        goals.forEach(goal -> {
            objectiveRepository.findByGoalIdOrderBySortOrder(goal.getId()).forEach(obj -> {
                Row row = sheet.createRow(rowNum.getAndIncrement());
                row.createCell(0).setCellValue(obj.getId());
                row.createCell(1).setCellValue(goal.getId());
                row.createCell(2).setCellValue(obj.getTitle());
                row.createCell(3).setCellValue(obj.getDescription() != null ? obj.getDescription() : "");
                row.createCell(4).setCellValue(Boolean.TRUE.equals(obj.getFrozen()) ? "Yes" : "No");

                List<Long> univIds = objectiveMappingRepository.findByDeptObjectiveId(obj.getId())
                        .stream().map(om -> om.getUniversityObjective().getId()).toList();
                row.createCell(5).setCellValue(univIds.isEmpty() ? "" : univIds.toString());
            });
        });

        autoSizeColumns(sheet, 6);
    }

    private void createInitiativesSheet(Workbook wb, Strategy strategy, CellStyle headerStyle) {
        Sheet sheet = wb.createSheet("Initiatives");
        Row hdr = sheet.createRow(0);
        createCell(hdr, 0, "Initiative ID", headerStyle);
        createCell(hdr, 1, "Objective ID", headerStyle);
        createCell(hdr, 2, "Title", headerStyle);
        createCell(hdr, 3, "Description", headerStyle);
        createCell(hdr, 4, "University Initiative ID (if dept)", headerStyle);

        AtomicInteger rowNum = new AtomicInteger(1);
        List<Initiative> initiatives = initiativeRepository.findByStrategyId(strategy.getId());
        initiatives.forEach(init -> {
            Row row = sheet.createRow(rowNum.getAndIncrement());
            row.createCell(0).setCellValue(init.getId());
            row.createCell(1).setCellValue(init.getObjective().getId());
            row.createCell(2).setCellValue(init.getTitle());
            row.createCell(3).setCellValue(init.getDescription() != null ? init.getDescription() : "");

            Long univInitId = initiativeMappingRepository.findByDeptInitiativeId(init.getId())
                    .map(im -> im.getUniversityInitiative().getId()).orElse(null);
            row.createCell(4).setCellValue(univInitId != null ? String.valueOf(univInitId) : "");
        });

        autoSizeColumns(sheet, 5);
    }

    private void createMeasurementsSheet(Workbook wb, Strategy strategy, CellStyle headerStyle) {
        Sheet sheet = wb.createSheet("Measurements");
        Row hdr = sheet.createRow(0);
        createCell(hdr, 0, "Measurement ID", headerStyle);
        createCell(hdr, 1, "Initiative ID", headerStyle);
        createCell(hdr, 2, "Description", headerStyle);
        createCell(hdr, 3, "Unit", headerStyle);
        createCell(hdr, 4, "Target Value", headerStyle);
        createCell(hdr, 5, "Actual Value", headerStyle);

        AtomicInteger rowNum = new AtomicInteger(1);
        measurementRepository.findByStrategyId(strategy.getId()).forEach(m -> {
            Row row = sheet.createRow(rowNum.getAndIncrement());
            row.createCell(0).setCellValue(m.getId());
            row.createCell(1).setCellValue(m.getInitiative().getId());
            row.createCell(2).setCellValue(m.getDescription());
            row.createCell(3).setCellValue(m.getUnit() != null ? m.getUnit() : "");
            row.createCell(4).setCellValue(m.getTargetValue() != null ? m.getTargetValue().doubleValue() : 0);
            row.createCell(5).setCellValue(m.getActualValue() != null ? m.getActualValue().doubleValue() : 0);
        });

        autoSizeColumns(sheet, 6);
    }

    private void createAchievementsSheet(Workbook wb, Strategy strategy, CellStyle headerStyle) {
        Sheet sheet = wb.createSheet("Achievements");
        Row hdr = sheet.createRow(0);
        createCell(hdr, 0, "Achievement ID", headerStyle);
        createCell(hdr, 1, "Measurement ID", headerStyle);
        createCell(hdr, 2, "Title", headerStyle);
        createCell(hdr, 3, "Type", headerStyle);
        createCell(hdr, 4, "Details", headerStyle);
        createCell(hdr, 5, "Author", headerStyle);
        createCell(hdr, 6, "Assessment Period", headerStyle);
        createCell(hdr, 7, "Recorded At", headerStyle);

        AtomicInteger rowNum = new AtomicInteger(1);
        List<Achievement> achievements;

        if (strategy.getStrategyType() == StrategyType.UNIVERSITY) {
            List<Initiative> initiatives = initiativeRepository.findByStrategyId(strategy.getId());
            achievements = initiatives.stream()
                    .flatMap(i -> achievementRepository.findAggregatedByUniversityInitiativeId(i.getId()).stream())
                    .toList();
        } else {
            List<Measurement> measurements = measurementRepository.findByStrategyId(strategy.getId());
            achievements = measurements.stream()
                    .flatMap(m -> achievementRepository.findByMeasurementIdOrderByRecordedAtDesc(m.getId()).stream())
                    .toList();
        }

        achievements.forEach(a -> {
            Row row = sheet.createRow(rowNum.getAndIncrement());
            row.createCell(0).setCellValue(a.getId());
            row.createCell(1).setCellValue(a.getMeasurement().getId());
            row.createCell(2).setCellValue(a.getTitle());
            row.createCell(3).setCellValue(a.getAchievementType().getName());
            row.createCell(4).setCellValue(a.getDetails() != null ? a.getDetails() : "");
            row.createCell(5).setCellValue(a.getAuthor().getFname() + " " + a.getAuthor().getLname());
            row.createCell(6).setCellValue(a.getAssessmentPeriod() != null ? a.getAssessmentPeriod().getName() : "");
            row.createCell(7).setCellValue(a.getRecordedAt().toString());
        });

        autoSizeColumns(sheet, 8);
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void autoSizeColumns(Sheet sheet, int numCols) {
        for (int i = 0; i < numCols; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
