package com.rit.spms.service;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.rit.spms.domain.*;
import com.rit.spms.domain.enums.StrategyType;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/** Builds downloadable PDFs (iText 7) for a Strategy's tree/achievements and for a concluded Annual Evaluation. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PdfExportService {

    private final StrategyRepository strategyRepository;
    private final VisionAreaRepository visionAreaRepository;
    private final GoalRepository goalRepository;
    private final ObjectiveRepository objectiveRepository;
    private final InitiativeRepository initiativeRepository;
    private final MeasurementRepository measurementRepository;
    private final AchievementRepository achievementRepository;
    private final PermissionService permissionService;
    private final AnnualEvaluationRepository annualEvaluationRepository;
    private final AnnualEvaluationCategoryResultRepository annualEvaluationCategoryResultRepository;
    private final AnnualEvaluationCriteriaResultRepository annualEvaluationCriteriaResultRepository;
    private final AnnualEvaluationGoalResultRepository annualEvaluationGoalResultRepository;
    private final CategoryCriteriaRepository categoryCriteriaRepository;
    private final TitleRankLabelRepository titleRankLabelRepository;
    private final PortfolioEntryRepository portfolioEntryRepository;

    // ── palette ───────────────────────────────────────────────────────────────

    private static final DeviceRgb NAVY        = new DeviceRgb(19,  34,  58);
    private static final DeviceRgb NAVY_MID    = new DeviceRgb(42,  82,  152);
    private static final DeviceRgb NAVY_LITE   = new DeviceRgb(232, 238, 246);
    private static final DeviceRgb GRAY_BG     = new DeviceRgb(248, 249, 250);
    private static final DeviceRgb GRAY_TEXT   = new DeviceRgb(107, 114, 128);
    private static final DeviceRgb GREEN       = new DeviceRgb(82,  196, 26);
    private static final DeviceRgb AMBER       = new DeviceRgb(250, 140, 22);
    private static final DeviceRgb RED_C       = new DeviceRgb(255, 77,  79);

    // ── status helpers ────────────────────────────────────────────────────────

    private String mark(long count, int threshold) {
        if (count == 0) return "✗";
        if (count >= threshold) return "✓";
        return "~";
    }

    private DeviceRgb statusFg(long count, int threshold) {
        if (count == 0) return RED_C;
        if (count >= threshold) return GREEN;
        return AMBER;
    }

    private long iniCount(Initiative ini, boolean isUniversity) {
        return isUniversity
                ? achievementRepository.countAggregatedByUniversityInitiativeId(ini.getId())
                : achievementRepository.countByMeasurementInitiativeId(ini.getId());
    }

    // ── public entry ──────────────────────────────────────────────────────────

    public byte[] exportStrategy(Long strategyId, Long currentUserId) {
        permissionService.assertCanRead(currentUserId, strategyId);

        Strategy strategy = strategyRepository.findById(Objects.requireNonNull(strategyId))
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));

        int threshold = strategy.getAchievementThreshold() != null
                ? strategy.getAchievementThreshold() : 3;
        boolean isUniversity = strategy.getStrategyType() == StrategyType.UNIVERSITY;
        String generated = DateTimeFormatter
                .ofPattern("MMMM d, yyyy 'at' HH:mm")
                .format(LocalDateTime.now());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf   = new PdfDocument(writer);
             Document   doc    = new Document(pdf)) {

            doc.setMargins(40, 40, 40, 40);

            // ── cover header ─────────────────────────────────────────────────

            doc.add(new Paragraph(strategy.getTitle())
                    .setFontSize(22).setBold()
                    .setFontColor(NAVY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5));

            String typeLabel = isUniversity ? "University Strategy" :
                    "Department Strategy"
                    + (strategy.getDepartment() != null
                       ? " — " + strategy.getDepartment().getName() : "");
            doc.add(new Paragraph(typeLabel + "  ·  " + strategy.getPlanningCycle().getName())
                    .setFontSize(12)
                    .setFontColor(GRAY_TEXT)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(3));

            doc.add(new Paragraph(
                    "Status: " + strategy.getState()
                    + "  ·  Achievement threshold: " + threshold + " per initiative")
                    .setFontSize(11)
                    .setFontColor(GRAY_TEXT)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(3));

            doc.add(new Paragraph("Report generated: " + generated)
                    .setFontSize(10)
                    .setItalic()
                    .setFontColor(GRAY_TEXT)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(14));

            doc.add(new LineSeparator(new SolidLine()).setMarginBottom(10));

            // ── legend ────────────────────────────────────────────────────────

            Table legend = new Table(UnitValue.createPercentArray(new float[]{34, 33, 33}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(18);
            legend.addCell(legendCell("✓", GREEN,  "≥ " + threshold + " achievements — on track"));
            legend.addCell(legendCell("~", AMBER, "1–" + (threshold - 1) + " achievements — partial"));
            legend.addCell(legendCell("✗", RED_C,  "0 achievements — not started"));
            doc.add(legend);

            // ── goals grouped by vision area ──────────────────────────────────

            List<VisionArea> areas = visionAreaRepository.findByStrategyIdOrderBySortOrder(strategyId);
            List<Goal>       allGoals = goalRepository.findByStrategyIdOrderBySortOrder(strategyId);

            Map<Long, List<Goal>> byArea = allGoals.stream()
                    .filter(g -> g.getArea() != null)
                    .collect(Collectors.groupingBy(
                            g -> g.getArea().getId(),
                            LinkedHashMap::new,
                            Collectors.toList()));

            List<Goal> ungrouped = allGoals.stream()
                    .filter(g -> g.getArea() == null)
                    .collect(Collectors.toList());

            for (VisionArea area : areas) {
                List<Goal> areaGoals = byArea.getOrDefault(area.getId(), Collections.emptyList());
                if (areaGoals.isEmpty()) continue;
                areaHeader(doc, area.getName());
                renderGoals(doc, areaGoals, isUniversity, threshold);
            }

            if (!ungrouped.isEmpty()) {
                if (!areas.isEmpty()) areaHeader(doc, "Other Goals");
                renderGoals(doc, ungrouped, isUniversity, threshold);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }

    // ── Annual Evaluation report ──────────────────────────────────────────────

    public byte[] exportAnnualEvaluation(Long evaluationId, Long currentUserId) {
        AnnualEvaluation evaluation = annualEvaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("AnnualEvaluation", evaluationId));
        permissionService.assertCanViewEvaluationReport(currentUserId, evaluation);
        if (evaluation.getState() != com.rit.spms.domain.enums.AnnualEvaluationState.CONCLUDED) {
            throw new com.rit.spms.exception.BusinessRuleException(
                    "A report can only be generated once the evaluation is concluded");
        }

        AppUser employee = evaluation.getEmployee();
        AppUser head = evaluation.getHead();
        String generated = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm").format(LocalDateTime.now());
        DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm");

        List<AnnualEvaluationCategoryResult> categoryResults = annualEvaluationCategoryResultRepository.findByEvaluationId(evaluationId);
        List<AnnualEvaluationCriteriaResult> criteriaResults = annualEvaluationCriteriaResultRepository.findByEvaluationId(evaluationId);
        List<AnnualEvaluationGoalResult> goalResults = annualEvaluationGoalResultRepository.findByEvaluationId(evaluationId);
        List<PortfolioEntry> entries = portfolioEntryRepository.findByEmployeeIdOrderByCreatedAtDesc(employee.getId());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf   = new PdfDocument(writer);
             Document   doc    = new Document(pdf)) {

            doc.setMargins(40, 40, 40, 40);

            doc.add(new Paragraph("Annual Evaluation")
                    .setFontSize(22).setBold().setFontColor(NAVY)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(5));
            doc.add(new Paragraph(employee.getFname() + " " + employee.getLname()
                    + (employee.getTitle() != null ? "  ·  " + employee.getTitle() : "")
                    + "  ·  " + evaluation.getAcademicYear().getName())
                    .setFontSize(13).setFontColor(GRAY_TEXT)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(3));
            doc.add(new Paragraph("Evaluated by: " + head.getFname() + " " + head.getLname())
                    .setFontSize(11).setFontColor(GRAY_TEXT)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(3));
            doc.add(new Paragraph("Report generated: " + generated)
                    .setFontSize(10).setItalic().setFontColor(GRAY_TEXT)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(14));
            doc.add(new LineSeparator(new SolidLine()).setMarginBottom(10));

            for (AnnualEvaluationCategoryResult catResult : categoryResults) {
                PortfolioCategory category = catResult.getCategory();
                Long titleId = category.getTitle().getId();

                areaHeader(doc, category.getCategoryName());
                doc.add(new Paragraph("Self-assessment: " + rankText(titleId, catResult.getEmployeeSelfRank())
                        + "     Head rank: " + rankText(titleId, catResult.getHeadCategoryRank()))
                        .setFontSize(11).setBold().setFontColor(NAVY_MID).setMarginBottom(6));
                if (catResult.getHeadComments() != null && !catResult.getHeadComments().isBlank()) {
                    doc.add(new Paragraph("Head comments: " + catResult.getHeadComments())
                            .setFontSize(9).setItalic().setFontColor(GRAY_TEXT).setMarginBottom(6));
                }

                for (CategoryCriteria criteria : categoryCriteriaRepository.findByCategoryIdOrderBySortOrder(category.getId())) {
                    Integer headRank = criteriaResults.stream()
                            .filter(r -> r.getCriteria().getId().equals(criteria.getId()))
                            .map(AnnualEvaluationCriteriaResult::getHeadRank)
                            .findFirst().orElse(null);

                    doc.add(new Paragraph("Criterion: " + criteria.getCriteriaName() + "  —  " + rankText(titleId, headRank))
                            .setFontSize(10).setBold().setFontColor(ColorConstants.BLACK)
                            .setMarginLeft(6).setMarginTop(4).setMarginBottom(1));

                    List<PortfolioEntry> criteriaEntries = entries.stream()
                            .filter(e -> e.getCriteria() != null && e.getCriteria().getId().equals(criteria.getId()))
                            .toList();
                    doc.add(new Paragraph("Criterion Achievements:")
                            .setFontSize(9).setBold().setFontColor(GRAY_TEXT)
                            .setMarginLeft(12).setMarginBottom(1));
                    if (criteriaEntries.isEmpty()) {
                        doc.add(new Paragraph("No achievements tagged to this criteria.")
                                .setFontSize(9).setItalic().setFontColor(GRAY_TEXT)
                                .setMarginLeft(18).setMarginBottom(2));
                    }
                    for (PortfolioEntry entry : criteriaEntries) {
                        doc.add(new Paragraph("•  " + entry.getAchievement().getTitle()
                                + "  (" + entry.getAchievement().getRecordedAt().format(df) + ")")
                                .setFontSize(9).setFontColor(GRAY_TEXT)
                                .setMarginLeft(18).setMarginBottom(2));
                    }
                }
            }

            if (!goalResults.isEmpty()) {
                Long titleIdForGoals = categoryResults.isEmpty() ? null : categoryResults.get(0).getCategory().getTitle().getId();
                areaHeader(doc, "Annual Goals");
                doc.add(new Paragraph("Self-assessment: " + rankText(titleIdForGoals, evaluation.getGoalsEmployeeSelfRank())
                                + "   |   Head rank: " + rankText(titleIdForGoals, evaluation.getGoalsHeadRank()))
                        .setFontSize(11).setBold().setFontColor(NAVY_MID).setMarginBottom(6));
                for (AnnualEvaluationGoalResult goalResult : goalResults) {
                    EmployeeGoal goal = goalResult.getGoal();
                    doc.add(new Paragraph("Goal: " + goal.getGoalTitle() + "  —  " + rankText(titleIdForGoals, goalResult.getHeadGoalRank()))
                            .setFontSize(10).setBold().setFontColor(ColorConstants.BLACK)
                            .setMarginLeft(6).setMarginTop(4).setMarginBottom(1));

                    List<PortfolioEntry> goalEntries = entries.stream()
                            .filter(e -> e.getGoal() != null && e.getGoal().getId().equals(goal.getId()))
                            .toList();
                    doc.add(new Paragraph("Goal Achievements:")
                            .setFontSize(9).setBold().setFontColor(GRAY_TEXT)
                            .setMarginLeft(12).setMarginBottom(1));
                    if (goalEntries.isEmpty()) {
                        doc.add(new Paragraph(Boolean.TRUE.equals(goalResult.getEmployeeNothingToReport())
                                        ? "Employee reported nothing for this goal." : "No achievements tagged to this goal.")
                                .setFontSize(9).setItalic().setFontColor(GRAY_TEXT)
                                .setMarginLeft(18).setMarginBottom(2));
                    }
                    for (PortfolioEntry entry : goalEntries) {
                        doc.add(new Paragraph("•  " + entry.getAchievement().getTitle()
                                + "  (" + entry.getAchievement().getRecordedAt().format(df) + ")")
                                .setFontSize(9).setFontColor(GRAY_TEXT)
                                .setMarginLeft(18).setMarginBottom(2));
                    }
                }
                if (evaluation.getGoalsHeadComments() != null && !evaluation.getGoalsHeadComments().isBlank()) {
                    doc.add(new Paragraph("Head comments: " + evaluation.getGoalsHeadComments())
                            .setFontSize(9).setItalic().setFontColor(GRAY_TEXT).setMarginBottom(6));
                }
            }

            doc.add(new LineSeparator(new SolidLine()).setMarginTop(14).setMarginBottom(10));
            doc.add(new Paragraph("Overall Annual Performance Rank: " + rankText(
                    categoryResults.isEmpty() ? null : categoryResults.get(0).getCategory().getTitle().getId(),
                    evaluation.getHeadOverallRank()))
                    .setFontSize(13).setBold().setFontColor(NAVY).setMarginBottom(10));

            doc.add(new Paragraph("Head signature: " + (evaluation.getHeadSignedAt() != null
                    ? "Signed by " + evaluation.getHeadSignatureName() + " on " + evaluation.getHeadSignedAt().format(df)
                    : "Not signed"))
                    .setFontSize(10).setFontColor(GRAY_TEXT).setMarginBottom(2));
            if (Boolean.TRUE.equals(evaluation.getEmployeeRefused())) {
                doc.add(new Paragraph("Employee: Refused to sign — " + evaluation.getEmployeeRefusalRationale())
                        .setFontSize(10).setFontColor(RED_C).setMarginBottom(2));
            } else {
                doc.add(new Paragraph("Employee signature: " + (evaluation.getEmployeeSignedAt() != null
                        ? "Signed by " + evaluation.getEmployeeSignatureName() + " on " + evaluation.getEmployeeSignedAt().format(df)
                        : "Not signed"))
                        .setFontSize(10).setFontColor(GRAY_TEXT).setMarginBottom(2));
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }

    private String rankText(Long titleId, Integer rank) {
        if (rank == null) return "—";
        if (titleId == null) return String.valueOf(rank);
        return titleRankLabelRepository.findByTitleIdAndRank(titleId, rank)
                .map(l -> rank + " – " + l.getLabel())
                .orElse(String.valueOf(rank));
    }

    // ── section helpers ───────────────────────────────────────────────────────

    private void areaHeader(Document doc, String name) {
        doc.add(new Paragraph(name)
                .setFontSize(13).setBold()
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(NAVY)
                .setPaddingLeft(10).setPaddingRight(10)
                .setPaddingTop(7).setPaddingBottom(7)
                .setMarginTop(18).setMarginBottom(6));
    }

    private void renderGoals(Document doc, List<Goal> goals,
                              boolean isUniversity, int threshold) {
        for (Goal goal : goals) {
            List<Objective> objectives =
                    objectiveRepository.findByGoalIdOrderBySortOrder(goal.getId());

            // pre-fetch counts for rollup
            long goalTotal = 0;
            Map<Long, Long>           objCounts = new LinkedHashMap<>();
            Map<Long, List<Initiative>> objInis = new LinkedHashMap<>();

            for (Objective obj : objectives) {
                List<Initiative> inis =
                        initiativeRepository.findByObjectiveIdOrderBySortOrder(obj.getId());
                long objTotal = 0;
                for (Initiative ini : inis) {
                    long c = iniCount(ini, isUniversity);
                    objTotal   += c;
                    goalTotal  += c;
                }
                objCounts.put(obj.getId(), objTotal);
                objInis.put(obj.getId(), inis);
            }

            // goal row
            String goalLabel = (goal.getTheme() != null
                    ? "[" + goal.getTheme().getName() + "]  " : "")
                    + goal.getTitle();
            doc.add(statusRow(goalLabel, goalTotal, threshold,
                    14f, true, NAVY_LITE, NAVY_MID, 0));

            if (goal.getDescription() != null && !goal.getDescription().isBlank()) {
                doc.add(new Paragraph(goal.getDescription())
                        .setFontSize(9).setFontColor(GRAY_TEXT)
                        .setItalic().setMarginLeft(6).setMarginTop(1).setMarginBottom(4));
            }

            for (Objective obj : objectives) {
                long objTotal = objCounts.getOrDefault(obj.getId(), 0L);
                List<Initiative> inis = objInis.getOrDefault(obj.getId(), Collections.emptyList());
                if (inis.isEmpty()) continue;

                // objective row
                doc.add(statusRow("  Objective: " + obj.getTitle(), objTotal, threshold,
                        11f, true, GRAY_BG, NAVY_MID, 10));

                for (Initiative ini : inis) {
                    long iniTotal = iniCount(ini, isUniversity);
                    renderInitiative(doc, ini, iniTotal, isUniversity, threshold);
                }
            }
        }
    }

    private void renderInitiative(Document doc, Initiative ini, long count,
                                   boolean isUniversity, int threshold) {
        // initiative row
        doc.add(statusRow("      ●  " + ini.getTitle(), count, threshold,
                10f, false, null, ColorConstants.BLACK, 0));

        // KPIs (measurements) this initiative is tracked against — rendered before the
        // achievement evidence below, same order as the on-screen strategy tree.
        List<Measurement> measurements =
                measurementRepository.findByInitiativeIdOrderBySortOrder(ini.getId());
        for (Measurement m : measurements) {
            renderMeasurement(doc, m);
        }

        if (isUniversity) {
            List<Object[]> rows = achievementRepository
                    .countByPeriodAndDepartmentForUniversityInitiative(ini.getId());
            if (!rows.isEmpty()) {
                // Periods sorted alphabetically
                List<String> periods = rows.stream()
                        .map(r -> (String) r[0]).distinct().sorted()
                        .collect(Collectors.toList());

                // Departments ordered by total contributions descending
                Map<String, Long> deptTotals = new LinkedHashMap<>();
                for (Object[] r : rows) {
                    String dept = (String) r[1];
                    long   c    = ((Number) r[2]).longValue();
                    deptTotals.put(dept, deptTotals.getOrDefault(dept, 0L) + c);
                }
                List<String> depts = deptTotals.entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

                // Build period → dept → count grid
                Map<String, Map<String, Long>> grid = new LinkedHashMap<>();
                for (Object[] r : rows)
                    grid.computeIfAbsent((String) r[0], k -> new LinkedHashMap<>())
                        .put((String) r[1], ((Number) r[2]).longValue());

                // Pivot table: first column = Period, one column per department
                int     n      = depts.size();
                float[] widths = new float[n + 1];
                widths[0] = Math.max(14f, 70f / (n + 1));
                for (int i = 1; i <= n; i++) widths[i] = (100f - widths[0]) / n;

                Table pt = new Table(UnitValue.createPercentArray(widths))
                        .setWidth(UnitValue.createPercentValue(100))
                        .setMarginBottom(6).setFontSize(8);

                pt.addHeaderCell(hdrCell("Period"));
                for (String dept : depts) pt.addHeaderCell(hdrCellCenter(dept));

                for (String period : periods) {
                    Map<String, Long> dc = grid.getOrDefault(period, Collections.emptyMap());
                    pt.addCell(dataCell(period));
                    for (String dept : depts) {
                        long c = dc.getOrDefault(dept, 0L);
                        pt.addCell(dataCellCenter(c > 0 ? String.valueOf(c) : "—"));
                    }
                }
                doc.add(pt);
            }
        } else {
            // department strategy: achievements grouped by period
            List<Achievement> achievements =
                    achievementRepository.findByInitiativeId(ini.getId());
            if (!achievements.isEmpty()) {
                Map<String, List<Achievement>> byPeriod = achievements.stream()
                        .collect(Collectors.groupingBy(
                                a -> a.getAssessmentPeriod() != null
                                        ? a.getAssessmentPeriod().getName() : "Unassigned",
                                LinkedHashMap::new, Collectors.toList()));

                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                for (Map.Entry<String, List<Achievement>> entry : byPeriod.entrySet()) {
                    doc.add(new Paragraph(
                            "              " + entry.getKey()
                            + "  (" + entry.getValue().size() + " achievement"
                            + (entry.getValue().size() != 1 ? "s" : "") + ")")
                            .setFontSize(9).setFontColor(NAVY_MID).setBold()
                            .setMarginTop(2).setMarginBottom(1));

                    for (Achievement a : entry.getValue()) {
                        String line = "                  –  ["
                                + a.getAchievementType().getName() + "]  "
                                + a.getTitle()
                                + "  (" + a.getAuthor().getFname() + " "
                                + a.getAuthor().getLname()
                                + ", " + a.getRecordedAt().format(df) + ")";
                        doc.add(new Paragraph(line)
                                .setFontSize(8.5f).setFontColor(GRAY_TEXT)
                                .setMarginTop(0).setMarginBottom(2));
                    }
                }
            }
        }
    }

    /** One KPI line: description plus target/actual, actual color-coded same as the on-screen ratio (green/amber/red). */
    private void renderMeasurement(Document doc, Measurement m) {
        String unit = m.getUnit() != null && !m.getUnit().isBlank() ? " " + m.getUnit() : "";
        String target = m.getTargetValue() != null ? m.getTargetValue().toPlainString() : "—";
        String actual = m.getActualValue() != null ? m.getActualValue().toPlainString() : "—";

        DeviceRgb actualColor = GRAY_TEXT;
        if (m.getTargetValue() != null && m.getTargetValue().signum() > 0 && m.getActualValue() != null) {
            double ratio = m.getActualValue().doubleValue() / m.getTargetValue().doubleValue();
            actualColor = ratio >= 1 ? GREEN : ratio >= 0.7 ? AMBER : RED_C;
        }

        Paragraph line = new Paragraph()
                .add(new Text("                  ◦  " + m.getDescription() + "   ").setFontColor(NAVY_MID))
                .add(new Text("Target: " + target + unit + "   ").setFontColor(GRAY_TEXT))
                .add(new Text("Actual: " + actual + unit).setFontColor(actualColor).setBold())
                .setFontSize(8.5f)
                .setMarginTop(0).setMarginBottom(2);
        doc.add(line);
    }

    // ── cell / row builders ───────────────────────────────────────────────────

    /**
     * Two-column row: left=label, right=status badge (right-aligned).
     * Background only applied when bg != null.
     */
    private Table statusRow(String label, long count, int threshold,
                             float fontSize, boolean bold,
                             DeviceRgb bg, Color labelColor, float leftPad) {
        String badge = mark(count, threshold) + "  " + count;
        DeviceRgb badgeColor = statusFg(count, threshold);

        Paragraph leftP = new Paragraph(label)
                .setFontSize(fontSize)
                .setFontColor(labelColor)
                .setMultipliedLeading(1.2f);
        if (bold) leftP.setBold();

        Paragraph rightP = new Paragraph(badge)
                .setFontSize(fontSize)
                .setFontColor(badgeColor)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMultipliedLeading(1.2f);
        if (bold) rightP.setBold();

        Cell leftCell  = new Cell().add(leftP) .setBorder(Border.NO_BORDER)
                .setPaddingLeft(6 + leftPad).setPaddingRight(4)
                .setPaddingTop(4).setPaddingBottom(4);
        Cell rightCell = new Cell().add(rightP).setBorder(Border.NO_BORDER)
                .setPaddingLeft(4).setPaddingRight(6)
                .setPaddingTop(4).setPaddingBottom(4);

        if (bg != null) {
            leftCell.setBackgroundColor(bg);
            rightCell.setBackgroundColor(bg);
        }

        return new Table(UnitValue.createPercentArray(new float[]{83, 17}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(4).setMarginBottom(2)
                .addCell(leftCell)
                .addCell(rightCell);
    }

    private Cell legendCell(String mark, DeviceRgb color, String label) {
        return new Cell().setBorder(Border.NO_BORDER).setPadding(3)
                .add(new Paragraph()
                        .add(new Text("  " + mark + "  ").setFontColor(color).setBold().setFontSize(10))
                        .add(new Text(label).setFontColor(GRAY_TEXT).setFontSize(9)));
    }

    private Cell hdrCell(String text) {
        return new Cell()
                .setBackgroundColor(NAVY_LITE)
                .setPadding(3)
                .add(new Paragraph(text).setFontSize(8).setBold().setFontColor(NAVY_MID));
    }

    private Cell hdrCellCenter(String text) {
        return new Cell()
                .setBackgroundColor(NAVY_LITE)
                .setPadding(3)
                .add(new Paragraph(text).setFontSize(8).setBold().setFontColor(NAVY_MID)
                        .setTextAlignment(TextAlignment.CENTER));
    }

    private Cell dataCell(String text) {
        return new Cell()
                .setPadding(3)
                .add(new Paragraph(text).setFontSize(8).setFontColor(GRAY_TEXT));
    }

    private Cell dataCellCenter(String text) {
        return new Cell()
                .setPadding(3)
                .add(new Paragraph(text).setFontSize(8).setFontColor(GRAY_TEXT)
                        .setTextAlignment(TextAlignment.CENTER));
    }
}
