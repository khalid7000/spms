package com.rit.spms.service;

import com.rit.spms.domain.CriteriaInfoToolAssignment;

import java.util.List;

/**
 * A pluggable, HEAD-ONLY viewer an Admin can wire to one CategoryCriteria per EmployeeTitle (see
 * PortfolioCategoryService's info-tool assignment methods) -- the read-only counterpart to {@link
 * CustomizableAchievementModule}. Where an achievement module lets the EMPLOYEE record achievements,
 * an info tool lets the EVALUATOR (head/chair, or anyone above them) pull in reference information
 * about a criterion for a specific employee, sourced however the implementation likes (the first,
 * {@code CentralRepositoryViewerTool}, reads back rows an Admin imported earlier). Adding a new tool
 * is just a new {@code @Service} implementing this interface -- Spring auto-collects every bean of
 * this type into {@link CriteriaInfoToolRegistry}, so no call site needs to change.
 *
 * <p>Unlike {@code CustomizableAchievementModule}, there is no {@code getDisplayName()} here -- per
 * product requirement, the label shown to the head is entirely Admin-configurable, stored on {@link
 * CriteriaInfoToolAssignment#getDisplayName()} rather than hardcoded in Java.
 *
 * <p>Methods are stateless (identity/employee info passed as parameters on every call) rather than
 * literally "initialized" as a stateful object, since Spring manages implementations as singleton
 * beans -- passing per-request state as parameters achieves the same practical effect without
 * fighting that lifecycle.
 */
public interface CriteriaInfoTool {

    /** Stable identifier persisted in criteria_info_tool_assignment.tool_code -- never change once shipped. */
    String getCode();

    /** Admin-facing helper text describing what this tool does and what it needs configured. */
    String getDescription();

    /** The filterable choices (e.g. terms) available for this employee under this assignment -- the head picks from these. */
    List<InfoOption> listAvailableOptions(CriteriaInfoToolAssignment assignment, String firstName, String lastName, String email);

    /** The free text to display for the given selected option keys. */
    String getDetails(CriteriaInfoToolAssignment assignment, String firstName, String lastName, String email, List<String> selectedOptionKeys);

    record InfoOption(String key, String label) {}
}
