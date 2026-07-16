package com.rit.spms.service;

/**
 * A pluggable achievement-recording helper an Admin can wire to one CategoryCriteria per
 * EmployeeTitle (see PortfolioCategoryService's achievement-module assignment methods). Adding a
 * new module is just a new {@code @Service} implementing this interface -- Spring auto-collects
 * every bean of this type into {@link CustomizableAchievementModuleRegistry}, so no call site
 * needs to change (same idiom as {@link NotificationChannel}). Each module owns its own dedicated
 * controller/service/entities for its actual recording flow; this interface only carries the
 * identity/display metadata the registry and the Admin assignment UI need.
 */
public interface CustomizableAchievementModule {

    /** Stable identifier persisted in criteria_achievement_module.module_code -- never change once shipped. */
    String getCode();

    /** Shown to the Admin when picking a module to assign to a criterion. */
    String getDisplayName();

    /** Shown on the employee-facing button that opens this module's recording interface. */
    String getButtonLabel();

    /** Admin-facing helper text describing what this module does. */
    String getDescription();
}
