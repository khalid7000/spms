package com.rit.spms.service;

/** Shared prompt construction for {@link TeachingEvaluationDraftGenerator} implementations. */
final class TeachingEvaluationAiPrompts {

    // Keeps a single prompt call well within any reasonable model context window, even a locally
    // hosted one -- truncated rather than rejected, so a large batch of evaluations still drafts
    // something from whatever fits.
    private static final int MAX_INPUT_CHARS = 40_000;

    private TeachingEvaluationAiPrompts() {
    }

    // The three-level recurrence scheme is identical for Strengths and Areas for Improvement, so
    // it's spelled out once and referenced from both section instructions below.
    private static final String RECURRENCE_RULES =
            "Tag each bullet with exactly one of these recurrence levels, based on the course "
            + "boundaries you identified above:\n"
            + "  * \"[Recurring within one course]\" -- multiple respondents within a SINGLE course "
            + "raise this same comment/theme, and it does not appear in any other course.\n"
            + "  * \"[Recurring across N courses]\" -- this same comment/theme appears in more than "
            + "one distinct course (use the real count for N), regardless of how many times it comes "
            + "up within any single course.\n"
            + "  * \"[Recurring within a course AND across N courses]\" -- the highest level: the "
            + "comment both repeats multiple times within at least one course AND appears across "
            + "multiple distinct courses (use the real count for N). Both kinds of recurrence matter "
            + "and this combined case is more significant than either alone.\n"
            + "  * No prefix at all -- the comment is a single, one-off mention in only one course.\n"
            + "Never guess or round a count -- only tag something as recurring if you can actually "
            + "point to it appearing more than once, and use the true number of occurrences/courses.";

    static String buildDraftPrompt(String extractedEvaluationText) {
        String text = extractedEvaluationText == null ? "" : extractedEvaluationText;
        if (text.length() > MAX_INPUT_CHARS) {
            text = text.substring(0, MAX_INPUT_CHARS) + "\n\n[...truncated...]";
        }
        return "You are helping a faculty member draft an achievement entry for their Annual Evaluation "
                + "portfolio, based on their own course evaluations for a teaching period.\n\n"
                + "Below is the raw text extracted from their uploaded course-evaluation files (student "
                + "comments, ratings, free-text feedback, etc.). Each file is marked with its own "
                + "\"=== filename ===\" header, but that is a FILE boundary only -- it is NOT the same as "
                + "a COURSE boundary. A single file (e.g. one PDF export) commonly contains many separate "
                + "course evaluations concatenated one after another, and the same course's evaluation "
                + "could even be split across files. Read through the ENTIRE text first and identify the "
                + "boundary of every individual course evaluation yourself, using cues like repeated "
                + "\"Course:\"/\"Section:\"/\"Instructor:\" headers, course code patterns (e.g. CS101, "
                + "ENGL-220), section numbers, and a survey question structure that repeats to signal a "
                + "new evaluation has started. Do not assume one file equals one course, and do not "
                + "assume the first course you see is the only one -- a single file may contain ten or "
                + "more distinct course evaluations, and you must find and use every one of them.\n\n"
                + "---\n" + text + "\n---\n\n"
                + "This text becomes the final, unedited record on the achievement -- the faculty member "
                + "cannot revise it afterward, so write it carefully and only from what's actually there. "
                + "Format it as plain text (no markdown headers, no code fences) with exactly three "
                + "sections, in this order:\n\n"
                + "Course Information:\n- Before writing anything, count how many distinct course "
                + "evaluations are actually present in the text above -- this is very often MORE THAN "
                + "ONE, sometimes ten or more concatenated together in a single file. Then list every "
                + "single one of them here, one line each, in the order they appear -- if you counted 9, "
                + "this list must have 9 lines, not 1. Do not stop after the first course, do not treat "
                + "the first course as a representative sample, and do not summarize -- list all of them "
                + "individually. For each line, give, in this exact order: the academic Term (e.g. \"Fall "
                + "2025\", \"Spring 2026\") that appears near the top of that course's evaluation, before "
                + "its course details; the Course Code; the Course Title; the word \"Section\" followed by "
                + "the section number; then \" - \" followed by the number of possible respondents and the "
                + "word \"Students\" (that count also appears near the top of each course's evaluation, in "
                + "a line like \"There were: 24 possible respondents\" -- use that number). For example: "
                + "\"Fall 2025 MATH 161 Applied Calculus Section 605 - 24 Students\". Determine each part "
                + "independently from the text (write \"not found\" for any part you can't determine -- "
                + "never invent a term, code, section, title, or respondent count). If a stretch of text "
                + "doesn't look like a course evaluation at all, say so instead of forcing it into this "
                + "list.\n\n"
                + "Strengths:\n- (2 to 4 bullet points, each one concise sentence, citing themes from the "
                + "actual feedback across the courses you identified). " + RECURRENCE_RULES + "\n\n"
                + "Areas for Improvement:\n- (2 to 4 bullet points, each one concise, constructive "
                + "sentence). " + RECURRENCE_RULES + " Separately, for any comment that raises a serious "
                + "concern (e.g. about fairness, workload, communication, or conduct -- as opposed to "
                + "routine constructive feedback), also prefix it with \"[Serious]\" in addition to "
                + "whichever recurrence tag (or lack of one) applies, so it reads clearly as either a "
                + "pattern across courses, a pattern within one course, or an isolated incident.\n\n"
                + "Write in the first person, as the faculty member describing their own teaching this "
                + "period. Base every point on what's actually in the text above -- don't invent specifics "
                + "that aren't supported by it.\n\n"
                + "Before you finish, double check: does Course Information list every distinct course "
                + "you actually found, not just the first one? If the source text contains multiple "
                + "courses and your list has only one entry, that is wrong -- go back and find the rest.";
    }
}
