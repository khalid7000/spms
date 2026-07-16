package com.rit.spms.dto.response;

import com.rit.spms.domain.OrganizationSetting;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrganizationSettingResponse {

    private String key;
    private String value;
    private String description;

    public static OrganizationSettingResponse from(OrganizationSetting s) {
        return OrganizationSettingResponse.builder()
                .key(s.getSettingKey())
                .value(s.getValue())
                .description(s.getDescription())
                .build();
    }
}
