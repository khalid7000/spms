package com.rit.spms.dto.response;

import com.rit.spms.domain.ImprovementTaskNote;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ImprovementTaskNoteResponse {
    private Long id;
    private Long authorId;
    private String authorName;
    private String body;
    private LocalDateTime createdAt;

    public static ImprovementTaskNoteResponse from(ImprovementTaskNote note) {
        return ImprovementTaskNoteResponse.builder()
                .id(note.getId())
                .authorId(note.getAuthor().getId())
                .authorName(note.getAuthor().getFname() + " " + note.getAuthor().getLname())
                .body(note.getBody())
                .createdAt(note.getCreatedAt())
                .build();
    }
}
