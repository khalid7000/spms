package com.rit.spms.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.rit.spms.domain.*;
import com.rit.spms.domain.enums.StrategyState;
import com.rit.spms.domain.enums.StrategyType;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PdfExportService {

    private final StrategyRepository strategyRepository;
    private final GoalRepository goalRepository;
    private final ObjectiveRepository objectiveRepository;
    private final InitiativeRepository initiativeRepository;
    private final MeasurementRepository measurementRepository;
    private final AchievementRepository achievementRepository;
    private final PermissionService permissionService;

    public byte[] exportStrategy(Long strategyId, Long currentUserId) {
        permissionService.assertOwner(currentUserId, strategyId);

        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf)) {

            // Title page
            doc.add(new Paragraph(strategy.getTitle())
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10));

            doc.add(new Paragraph(strategy.getPlanningCycle().getName() + " Cycle")
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setMarginBottom(5));

            String typeLabel = strategy.getStrategyType() == StrategyType.UNIVERSITY ? "University Strategy" :
                    "Department Strategy" + (strategy.getDepartment() != null ?
                            " — " + strategy.getDepartment().getName() : "");
            doc.add(new Paragraph(typeLabel)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5));

            doc.add(new Paragraph("State: " + strategy.getState())
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            if (strategy.getDescription() != null && !strategy.getDescription().isBlank()) {
                doc.add(new Paragraph(strategy.getDescription())
                        .setFontSize(11)
                        .setMarginBottom(20));
            }

            doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine()));

            // Goals
            List<Goal> goals = goalRepository.findByStrategyIdOrderBySortOrder(strategyId);
            for (Goal goal : goals) {
                String goalHeading = (goal.getTheme() != null ? "[" + goal.getTheme().getName() + "] " : "")
                        + "GOAL: " + goal.getTitle();
                doc.add(new Paragraph(goalHeading)
                        .setFontSize(14)
                        .setBold()
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setMarginTop(15)
                        .setMarginBottom(5)
                        .setPadding(5));

                if (goal.getDescription() != null && !goal.getDescription().isBlank()) {
                    doc.add(new Paragraph(goal.getDescription())
                            .setFontSize(10)
                            .setMarginBottom(8));
                }

                List<Objective> objectives = objectiveRepository.findByGoalIdOrderBySortOrder(goal.getId());
                for (Objective obj : objectives) {
                    String frozenMark = Boolean.TRUE.equals(obj.getFrozen()) ? " [FROZEN]" : "";
                    doc.add(new Paragraph("  Objective: " + obj.getTitle() + frozenMark)
                            .setFontSize(12)
                            .setBold()
                            .setMarginLeft(15)
                            .setMarginTop(8)
                            .setMarginBottom(4));

                    if (obj.getDescription() != null && !obj.getDescription().isBlank()) {
                        doc.add(new Paragraph(obj.getDescription())
                                .setFontSize(10)
                                .setMarginLeft(20)
                                .setMarginBottom(4));
                    }

                    List<Initiative> initiatives = initiativeRepository.findByObjectiveIdOrderBySortOrder(obj.getId());
                    for (Initiative init : initiatives) {
                        doc.add(new Paragraph("    • Initiative: " + init.getTitle())
                                .setFontSize(11)
                                .setMarginLeft(25)
                                .setMarginBottom(3));

                        if (init.getDescription() != null && !init.getDescription().isBlank()) {
                            doc.add(new Paragraph(init.getDescription())
                                    .setFontSize(10)
                                    .setMarginLeft(35)
                                    .setMarginBottom(3));
                        }

                        List<Measurement> measurements = measurementRepository
                                .findByInitiativeIdOrderBySortOrder(init.getId());
                        for (Measurement m : measurements) {
                            String kpiLine = "      KPI: " + m.getDescription()
                                    + (m.getUnit() != null ? " [" + m.getUnit() + "]" : "")
                                    + " | Target: " + (m.getTargetValue() != null ? m.getTargetValue() : "—")
                                    + " | Actual: " + (m.getActualValue() != null ? m.getActualValue() : "—");
                            doc.add(new Paragraph(kpiLine)
                                    .setFontSize(9)
                                    .setFontColor(ColorConstants.DARK_GRAY)
                                    .setMarginLeft(40)
                                    .setMarginBottom(2));

                            if (strategy.getState() == StrategyState.DEPLOYED
                                    || strategy.getState() == StrategyState.FROZEN) {

                                List<Achievement> achievements = strategy.getStrategyType() == StrategyType.UNIVERSITY
                                        ? achievementRepository.findAggregatedByUniversityInitiativeId(init.getId())
                                        : achievementRepository.findByMeasurementIdOrderByRecordedAtDesc(m.getId());

                                for (Achievement a : achievements) {
                                    String achLine = "        ✓ [" + a.getAchievementType().getName() + "] "
                                            + a.getTitle()
                                            + " — " + a.getAuthor().getFname() + " " + a.getAuthor().getLname()
                                            + " (" + a.getRecordedAt().toLocalDate() + ")";
                                    doc.add(new Paragraph(achLine)
                                            .setFontSize(9)
                                            .setFontColor(ColorConstants.BLUE)
                                            .setMarginLeft(50)
                                            .setMarginBottom(2));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }
}
