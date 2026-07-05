package com.rit.spms.dto.response;

import com.rit.spms.domain.AcademicYear;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
public class AcademicYearResponse {
    Long id;
    String name;
    LocalDate startDate;
    LocalDate endDate;
    Boolean closed;
    LocalDateTime createdAt;

    public static AcademicYearResponse from(AcademicYear y) {
        return AcademicYearResponse.builder()
                .id(y.getId())
                .name(y.getName())
                .startDate(y.getStartDate())
                .endDate(y.getEndDate())
                .closed(y.getClosed())
                .createdAt(y.getCreatedAt())
                .build();
    }
}
