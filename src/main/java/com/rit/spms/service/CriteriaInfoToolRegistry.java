package com.rit.spms.service;

import com.rit.spms.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** Looks up registered {@link CriteriaInfoTool} beans by their stable code. */
@Service
@RequiredArgsConstructor
public class CriteriaInfoToolRegistry {

    private final List<CriteriaInfoTool> tools;

    public List<CriteriaInfoTool> listAll() {
        return tools;
    }

    public CriteriaInfoTool require(String code) {
        return tools.stream()
                .filter(t -> t.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No such criteria info tool: " + code));
    }
}
