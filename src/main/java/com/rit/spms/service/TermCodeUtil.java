package com.rit.spms.service;

import com.rit.spms.exception.BusinessRuleException;

/**
 * Shared academic-term-code convention used by every {@link RepositoryReader} that keys its
 * records by term (first: {@link EarlyAlertReader}, {@link GradeDistributionReader}) -- drops the
 * character 3rd-from-right in the 4-digit year and appends a term suffix, e.g. "2025"+Fall ->
 * "225"+"1" = "2251". Readers that don't need a term concept at all simply don't use this.
 */
final class TermCodeUtil {

    private TermCodeUtil() {
    }

    static String computeTermCode(String year, String term) {
        String trimmedYear = year.substring(0, year.length() - 3) + year.substring(year.length() - 2);
        return trimmedYear + termSuffix(term);
    }

    static String computeTermLabel(String year, String term) {
        return termDisplayName(term) + " " + year;
    }

    private static String termSuffix(String term) {
        return switch (term.toUpperCase()) {
            case "FALL" -> "1";
            case "SPRING" -> "5";
            case "SUMMER" -> "8";
            default -> throw new BusinessRuleException("Unknown term: " + term + " (expected Fall/Spring/Summer)");
        };
    }

    private static String termDisplayName(String term) {
        return switch (term.toUpperCase()) {
            case "FALL" -> "Fall";
            case "SPRING" -> "Spring";
            case "SUMMER" -> "Summer";
            default -> throw new BusinessRuleException("Unknown term: " + term + " (expected Fall/Spring/Summer)");
        };
    }
}
