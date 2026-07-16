package com.rit.spms.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.rit.spms.domain.*;
import com.rit.spms.domain.enums.AnnualEvaluationState;
import com.rit.spms.exception.BusinessRuleException;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Owns the "Teaching Evaluations" achievement module's recording flow: the employee uploads their
 * course-evaluation files for a criterion (parsed to plain text on upload, binary discarded), the
 * AI drafts a "Details" blob from the accumulated text, and the employee reviews/edits before
 * finalizing into a normal {@link Achievement} + {@link PortfolioEntry} (not linked to the
 * Strategy Tree -- see the migration note on {@code Achievement.measurement}).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TeachingEvaluationSessionService {

    private final TeachingEvaluationSessionRepository sessionRepository;
    private final AnnualEvaluationRepository evaluationRepository;
    private final CategoryCriteriaRepository criteriaRepository;
    private final CriteriaAchievementModuleRepository moduleAssignmentRepository;
    private final AchievementRepository achievementRepository;
    private final AchievementTypeRepository achievementTypeRepository;
    private final AssessmentPeriodRepository assessmentPeriodRepository;
    private final PortfolioEntryRepository entryRepository;
    private final EmployeeGoalRepository goalRepository;
    private final AnnualEvaluationService annualEvaluationService;
    private final TeachingEvaluationDraftGenerator draftGenerator;

    public TeachingEvaluationSession getOrCreateSession(Long evaluationId, Long criteriaId, Long currentUserId) {
        AnnualEvaluation evaluation = requireOwnDraftEvaluation(evaluationId, currentUserId);
        CategoryCriteria criteria = requireAssignedCriteria(criteriaId);
        assertUnderLimit(evaluation, criteriaId);

        return sessionRepository.findByEvaluationIdAndCriteriaId(evaluationId, criteriaId)
                .orElseGet(() -> sessionRepository.save(TeachingEvaluationSession.builder()
                        .evaluation(evaluation)
                        .criteria(criteria)
                        .build()));
    }

    /**
     * Enforces the admin-configured "max achievements per academic year" for this module+criterion
     * (see {@link CriteriaAchievementModule#getMaxAchievementsPerYear()}) -- checked both when
     * opening/reopening a session ({@link #getOrCreateSession}) and again right before finalizing
     * ({@link #finalizeAchievement}, defense in depth against races or a stale multi-tab session),
     * so the tool becomes unusable for this criterion+year the instant the count is reached, not
     * just at the one call site. A not-yet-finalized session never counts against the limit itself
     * (it isn't a real {@link Achievement} until finalized), so an employee already partway through
     * recording their last allowed achievement can still finish it.
     */
    private void assertUnderLimit(AnnualEvaluation evaluation, Long criteriaId) {
        CriteriaAchievementModule assignment = moduleAssignmentRepository.findByCriteriaId(criteriaId).stream()
                .filter(a -> a.getModuleCode().equals(TeachingEvaluationsAchievementModule.CODE))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("This criterion does not have the Teaching Evaluations module assigned"));
        long countSoFar = entryRepository.countByEmployeeIdAndCriteriaIdAndModuleCodeAndPeriodName(
                evaluation.getEmployee().getId(), criteriaId, TeachingEvaluationsAchievementModule.CODE,
                evaluation.getAcademicYear().getName());
        int max = assignment.getMaxAchievementsPerYear();
        if (countSoFar >= max) {
            throw new BusinessRuleException("You've already recorded the maximum of " + max
                    + (max == 1 ? " achievement" : " achievements")
                    + " for this criterion this academic year via the Teaching Evaluations tool.");
        }
    }

    public TeachingEvaluationSession updateNote(Long sessionId, String note, Long currentUserId) {
        TeachingEvaluationSession session = requireSession(sessionId);
        requireOwnDraftEvaluation(session.getEvaluation().getId(), currentUserId);
        session.setLocalFolderNote(note);
        return sessionRepository.save(session);
    }

    /** Parses each uploaded file to plain text and appends it -- the binary itself is never stored. */
    public TeachingEvaluationSession uploadFiles(Long sessionId, List<MultipartFile> files, Long currentUserId) {
        TeachingEvaluationSession session = requireSession(sessionId);
        requireOwnDraftEvaluation(session.getEvaluation().getId(), currentUserId);
        if (files == null || files.isEmpty()) {
            throw new BusinessRuleException("Select at least one file to upload");
        }

        StringBuilder text = new StringBuilder(session.getExtractedText() != null ? session.getExtractedText() : "");
        List<String> fileNames = new ArrayList<>();
        if (session.getUploadedFileNames() != null && !session.getUploadedFileNames().isBlank()) {
            fileNames.add(session.getUploadedFileNames());
        }
        for (MultipartFile file : files) {
            String name = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            text.append(text.length() > 0 ? "\n\n" : "").append("=== ").append(name).append(" ===\n");
            text.append(extractText(name, file));
            fileNames.add(name);
        }

        session.setExtractedText(text.toString());
        session.setUploadedFileNames(String.join(", ", fileNames));
        // A new upload invalidates any prior draft -- it was based on incomplete text.
        session.setDraftDetails(null);
        session.setGeneratedAt(null);
        session.setGenerationFailureReason(null);
        return sessionRepository.save(session);
    }

    private String extractText(String fileName, MultipartFile file) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        try (InputStream in = file.getInputStream()) {
            if (lower.endsWith(".pdf")) {
                StringBuilder sb = new StringBuilder();
                try (PdfDocument pdf = new PdfDocument(new PdfReader(in))) {
                    for (int i = 1; i <= pdf.getNumberOfPages(); i++) {
                        PdfPage page = pdf.getPage(i);
                        sb.append(PdfTextExtractor.getTextFromPage(page)).append("\n");
                    }
                }
                return sb.toString();
            } else if (lower.endsWith(".docx")) {
                try (XWPFDocument doc = new XWPFDocument(in); XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                    return extractor.getText();
                }
            } else if (lower.endsWith(".txt")) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new BusinessRuleException("Could not read file '" + fileName + "': " + e.getMessage());
        }
        throw new BusinessRuleException("Unsupported file type for '" + fileName + "' -- only .pdf, .docx, and .txt are supported");
    }

    /**
     * Permission/state gate only -- deliberately NOT the place that calls
     * recordGenerationRequested/generateDraftAsync. Those two must be invoked directly from the
     * controller (a non-transactional caller), each getting its own transaction that commits
     * independently, exactly like every other AI-generation flow in this codebase (see
     * EmployeeGoalCycleController.generateSuggestions and the doc comment on
     * EmployeeGoalCycleService.assertCanGenerateSuggestions for the full commit-timing gotcha).
     */
    public TeachingEvaluationSession assertCanGenerateDraft(Long sessionId, Long currentUserId) {
        TeachingEvaluationSession session = requireSession(sessionId);
        requireOwnDraftEvaluation(session.getEvaluation().getId(), currentUserId);
        if (session.getExtractedText() == null || session.getExtractedText().isBlank()) {
            throw new BusinessRuleException("Upload at least one file before generating a draft");
        }
        return session;
    }

    public void recordGenerationRequested(Long sessionId) {
        TeachingEvaluationSession session = requireSession(sessionId);
        session.setGenerationRequestedAt(LocalDateTime.now());
        session.setGenerationFailureReason(null);
        sessionRepository.save(session);
    }

    @org.springframework.scheduling.annotation.Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateDraftAsync(Long sessionId) {
        TeachingEvaluationSession session = requireSession(sessionId);
        try {
            String draft = draftGenerator.generateDraft(session.getExtractedText());
            session.setDraftDetails(draft);
            session.setGeneratedAt(LocalDateTime.now());
            session.setGenerationFailureReason(null);
        } catch (Exception e) {
            session.setGenerationFailureReason(e.getMessage() != null ? e.getMessage() : "AI generation failed");
        }
        sessionRepository.save(session);
    }

    public void deleteSession(Long sessionId, Long currentUserId) {
        TeachingEvaluationSession session = requireSession(sessionId);
        requireOwnDraftEvaluation(session.getEvaluation().getId(), currentUserId);
        sessionRepository.delete(session);
    }

    /**
     * Creates the real Achievement + PortfolioEntry (measurement left null -- see the migration
     * note; this achievement is evaluation-only, not linked to the Strategy Tree), then discards
     * the session. Mirrors PortfolioEntryService.logAchievementWithEvaluation's
     * "clear stale nothing-to-report" follow-up.
     *
     * <p>The AI-generated review is never accepted from the client as free text -- it's always
     * read straight from {@code session.getDraftDetails()} so the record can't be edited or
     * fabricated client-side. Generating a draft is required before finalizing; the employee's
     * own required {@code reflection} is appended after it, both combined into the achievement's
     * single {@code details} field.
     */
    public PortfolioEntry finalizeAchievement(Long sessionId, String title, Long achievementTypeId, String customTypeName,
                                               String reflection, String privateNotes, Long goalId, Integer categoryRating,
                                               String evidenceUrl, Long currentUserId) {
        TeachingEvaluationSession session = requireSession(sessionId);
        AnnualEvaluation evaluation = requireOwnDraftEvaluation(session.getEvaluation().getId(), currentUserId);
        CategoryCriteria criteria = session.getCriteria();
        assertUnderLimit(evaluation, criteria.getId());

        if (title == null || title.isBlank()) {
            throw new BusinessRuleException("Title is required");
        }
        if (session.getGeneratedAt() == null || session.getDraftDetails() == null || session.getDraftDetails().isBlank()) {
            throw new BusinessRuleException("Generate the AI draft before saving this achievement");
        }
        if (reflection == null || reflection.isBlank()) {
            throw new BusinessRuleException("Add your reflection before saving this achievement");
        }
        if (evidenceUrl == null || evidenceUrl.isBlank()) {
            throw new BusinessRuleException("Evidence/Link is required");
        }
        if (categoryRating != null && (categoryRating < 1 || categoryRating > 5)) {
            throw new BusinessRuleException("Self-assessment rating must be between 1 and 5");
        }

        String details = "Generated AI Review:\n" + session.getDraftDetails()
                + "\n\nInstructor Reflection:\n" + reflection;

        AchievementType type = achievementTypeRepository.findById(achievementTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("AchievementType", achievementTypeId));
        boolean isOtherType = "Other".equalsIgnoreCase(type.getName());
        if (isOtherType && (customTypeName == null || customTypeName.isBlank())) {
            throw new BusinessRuleException("Describe the achievement type when selecting \"Other\"");
        }

        EmployeeGoal goal = null;
        if (goalId != null) {
            goal = goalRepository.findById(goalId)
                    .orElseThrow(() -> new ResourceNotFoundException("EmployeeGoal", goalId));
            if (!goal.getCycle().getEmployee().getId().equals(evaluation.getEmployee().getId())) {
                throw new BusinessRuleException("Goal does not belong to this employee");
            }
        }

        AssessmentPeriod period = assessmentPeriodRepository.findFirstByNameOrderByIdDesc(evaluation.getAcademicYear().getName())
                .orElse(null);

        Achievement achievement = achievementRepository.save(Achievement.builder()
                .measurement(null)
                .title(title)
                .achievementType(type)
                .customTypeName(isOtherType ? customTypeName : null)
                .details(details)
                .privateNotes(privateNotes)
                .author(evaluation.getEmployee())
                .assessmentPeriod(period)
                .createdByModuleCode(TeachingEvaluationsAchievementModule.CODE)
                .build());

        PortfolioEntry entry = entryRepository.save(PortfolioEntry.builder()
                .achievement(achievement)
                .employee(evaluation.getEmployee())
                .category(criteria.getCategory())
                .criteria(criteria)
                .goal(goal)
                .categoryRating(categoryRating)
                .evidenceUrl(evidenceUrl)
                .build());

        sessionRepository.delete(session);

        annualEvaluationService.clearNothingToReportContradiction(
                evaluation.getEmployee().getId(), period, criteria.getId(), goalId);

        return entry;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private TeachingEvaluationSession requireSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("TeachingEvaluationSession", sessionId));
    }

    private CategoryCriteria requireAssignedCriteria(Long criteriaId) {
        CategoryCriteria criteria = criteriaRepository.findById(criteriaId)
                .orElseThrow(() -> new ResourceNotFoundException("CategoryCriteria", criteriaId));
        boolean assigned = moduleAssignmentRepository.findByCriteriaId(criteriaId).stream()
                .anyMatch(a -> a.getModuleCode().equals(TeachingEvaluationsAchievementModule.CODE));
        if (!assigned) {
            throw new BusinessRuleException("This criterion does not have the Teaching Evaluations module assigned");
        }
        return criteria;
    }

    private AnnualEvaluation requireOwnDraftEvaluation(Long evaluationId, Long currentUserId) {
        AnnualEvaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("AnnualEvaluation", evaluationId));
        if (!evaluation.getEmployee().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Only this employee can record achievements on their own evaluation");
        }
        if (evaluation.getState() != AnnualEvaluationState.DRAFT
                && evaluation.getState() != AnnualEvaluationState.RETURNED_TO_EMPLOYEE) {
            throw new BusinessRuleException("Achievements can only be recorded while the evaluation is in DRAFT or returned to you for review");
        }
        return evaluation;
    }
}
