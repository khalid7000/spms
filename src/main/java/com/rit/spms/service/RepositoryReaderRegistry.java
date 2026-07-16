package com.rit.spms.service;

import com.rit.spms.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** Looks up registered {@link RepositoryReader} beans by their stable code. */
@Service
@RequiredArgsConstructor
public class RepositoryReaderRegistry {

    private final List<RepositoryReader> readers;

    public List<RepositoryReader> listAll() {
        return readers;
    }

    public RepositoryReader require(String code) {
        return readers.stream()
                .filter(r -> r.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No such repository reader: " + code));
    }
}
