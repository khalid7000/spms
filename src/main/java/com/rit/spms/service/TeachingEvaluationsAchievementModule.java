package com.rit.spms.service;

import org.springframework.stereotype.Service;

/**
 * First customizable achievement module: the employee uploads their actual course-evaluation
 * files (PDF/DOCX/TXT), the AI drafts a few strengths and areas-for-improvement into the
 * achievement's Details field, and the employee reviews/edits before saving. See
 * {@link TeachingEvaluationSessionService} for the actual recording flow.
 */
@Service
public class TeachingEvaluationsAchievementModule implements CustomizableAchievementModule {

    public static final String CODE = "TEACHING_EVALUATIONS";

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public String getDisplayName() {
        return "Teaching Evaluations";
    }

    @Override
    public String getButtonLabel() {
        return "My Course Teaching Evaluations Tool";
    }

    @Override
    public String getDescription() {
        return "Employee uploads their course-evaluation files for the period; AI drafts a few "
                + "strengths and areas for improvement into the achievement's Details field for review.";
    }
}
