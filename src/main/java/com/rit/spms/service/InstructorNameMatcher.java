package com.rit.spms.service;

import com.rit.spms.domain.AppUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared "resolve a spreadsheet's free-text instructor name column to an SPMS employee's email"
 * logic, used by every name-column-based {@link RepositoryReader} (first: {@link
 * EarlyAlertReader}, {@link GradeDistributionReader}). Source files disagree on both name order
 * ("Last, First" vs "First, Last" -- confirmed both occur in real exports) and comma spacing
 * ("Last, First" vs "Last,First" -- also confirmed both occur), so {@link #normalize} strips ALL
 * whitespace immediately around commas (not just collapses it) and matching tries both orders.
 */
final class InstructorNameMatcher {

    private InstructorNameMatcher() {
    }

    /** Keyed by both "last,first" and "first,last" (normalized) -> email, so callers can look up either order. */
    static Map<String, String> buildNameToEmailIndex(List<AppUser> users) {
        Map<String, String> index = new HashMap<>();
        for (AppUser user : users) {
            String firstLast = normalize(user.getFname() + "," + user.getLname());
            String lastFirst = normalize(user.getLname() + "," + user.getFname());
            index.putIfAbsent(firstLast, user.getEmail());
            index.putIfAbsent(lastFirst, user.getEmail());
        }
        return index;
    }

    static String resolveEmail(Map<String, String> nameToEmail, String rawName) {
        return nameToEmail.get(normalize(rawName));
    }

    static String normalize(String s) {
        return s.trim().toLowerCase()
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*,\\s*", ",");
    }
}
