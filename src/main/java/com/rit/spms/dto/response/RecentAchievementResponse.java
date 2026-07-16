package com.rit.spms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** One row in the Strategy Tree's "Recently logged" rail -- an achievement plus enough of its
 * own Initiative/Goal context to display without a second round trip. */
@Data
@Builder
public class RecentAchievementResponse {
    private Long id;
    private String title;
    private String achievementTypeName;
    private String authorName;
    private LocalDateTime recordedAt;
    private Long initiativeId;
    private String initiativeTitle;
    private String goalTitle;
}
