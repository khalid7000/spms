package com.rit.spms.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SwotJustificationResponse {
    private String contributorName;
    private String sentence;
}
