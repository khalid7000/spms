package com.rit.spms.service;

import com.rit.spms.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** Looks up registered {@link CustomizableAchievementModule} beans by their stable code. */
@Service
@RequiredArgsConstructor
public class CustomizableAchievementModuleRegistry {

    private final List<CustomizableAchievementModule> modules;

    public List<CustomizableAchievementModule> listAll() {
        return modules;
    }

    public CustomizableAchievementModule require(String code) {
        return modules.stream()
                .filter(m -> m.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No such achievement module: " + code));
    }
}
