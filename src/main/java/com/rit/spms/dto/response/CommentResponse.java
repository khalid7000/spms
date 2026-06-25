package com.rit.spms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private Long strategyId;
    private String entityType;
    private Long entityId;
    private Long authorId;
    private String authorName;
    private String content;
    private Long parentCommentId;
    private boolean unread;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
