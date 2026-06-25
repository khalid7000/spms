package com.rit.spms.controller;

import com.rit.spms.domain.AppUser;
import com.rit.spms.domain.Comment;
import com.rit.spms.domain.CommentRead;
import com.rit.spms.domain.Strategy;
import com.rit.spms.dto.request.CommentRequest;
import com.rit.spms.dto.response.ApiResponse;
import com.rit.spms.dto.response.CommentResponse;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.exception.UnauthorizedException;
import com.rit.spms.repository.AppUserRepository;
import com.rit.spms.repository.CommentReadRepository;
import com.rit.spms.repository.CommentRepository;
import com.rit.spms.repository.StrategyRepository;
import com.rit.spms.security.UserPrincipal;
import com.rit.spms.service.AuditService;
import com.rit.spms.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/strategies/{strategyId}/comments")
@RequiredArgsConstructor
@Transactional
public class CommentController {

    private final CommentRepository commentRepository;
    private final CommentReadRepository commentReadRepository;
    private final StrategyRepository strategyRepository;
    private final AppUserRepository appUserRepository;
    private final PermissionService permissionService;
    private final AuditService auditService;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable Long strategyId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId,
            @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanRead(principal.getId(), strategyId);

        List<Comment> comments = (entityType != null && entityId != null)
                ? commentRepository.findByStrategyIdAndEntityTypeAndEntityIdOrderByCreatedAtAsc(
                        strategyId, entityType, entityId)
                : commentRepository.findByStrategyIdOrderByCreatedAtAsc(strategyId);

        List<Long> ids = comments.stream().map(Comment::getId).toList();
        Set<Long> readIds = ids.isEmpty()
                ? Set.of()
                : commentReadRepository.findReadCommentIds(principal.getId(), ids);

        List<CommentResponse> response = comments.stream()
                .map(c -> toResponse(c, !readIds.contains(c.getId())))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long strategyId,
            @Valid @RequestBody CommentRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanComment(principal.getId(), strategyId);

        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy", strategyId));
        AppUser author = appUserRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", principal.getId()));

        Comment parent = null;
        if (req.getParentCommentId() != null) {
            parent = commentRepository.findById(req.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", req.getParentCommentId()));
            if (!parent.getStrategy().getId().equals(strategyId)) {
                throw new UnauthorizedException("Parent comment does not belong to this strategy");
            }
        }

        Comment comment = Comment.builder()
                .strategy(strategy)
                .entityType(req.getEntityType())
                .entityId(req.getEntityId())
                .author(author)
                .content(req.getContent())
                .parentComment(parent)
                .build();
        comment = commentRepository.save(comment);

        // Author has implicitly read their own comment
        commentReadRepository.save(CommentRead.builder()
                .comment(comment)
                .user(author)
                .build());

        auditService.log(author, "ADD_COMMENT", "Comment", comment.getId(), strategy,
                "Added comment on " + (req.getEntityType() != null ? req.getEntityType() : "strategy"));

        return ResponseEntity.status(201).body(ApiResponse.success("Comment added", toResponse(comment, false)));
    }

    /** Mark all comments on a specific element as read for the current user. */
    @PostMapping("/read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @PathVariable Long strategyId,
            @RequestParam String entityType,
            @RequestParam Long entityId,
            @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanRead(principal.getId(), strategyId);

        List<Comment> comments = commentRepository
                .findByStrategyIdAndEntityTypeAndEntityIdOrderByCreatedAtAsc(strategyId, entityType, entityId);
        AppUser user = appUserRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", principal.getId()));

        for (Comment c : comments) {
            if (!commentReadRepository.existsByCommentIdAndUserId(c.getId(), principal.getId())) {
                commentReadRepository.save(CommentRead.builder().comment(c).user(user).build());
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
    }

    /** Unread comment count for the current user across the whole strategy. */
    @GetMapping("/unread-count")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Long>> unreadCount(
            @PathVariable Long strategyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        permissionService.assertCanRead(principal.getId(), strategyId);
        long count = commentReadRepository.countUnreadForStrategy(strategyId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    private CommentResponse toResponse(Comment c, boolean unread) {
        AppUser author = c.getAuthor();
        return CommentResponse.builder()
                .id(c.getId())
                .strategyId(c.getStrategy().getId())
                .entityType(c.getEntityType())
                .entityId(c.getEntityId())
                .authorId(author.getId())
                .authorName(author.getFname() + " " + author.getLname())
                .content(c.getContent())
                .parentCommentId(c.getParentComment() != null ? c.getParentComment().getId() : null)
                .unread(unread)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
