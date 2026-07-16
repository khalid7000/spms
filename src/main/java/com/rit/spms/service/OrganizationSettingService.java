package com.rit.spms.service;

import com.rit.spms.domain.OrganizationSetting;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.OrganizationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * A small, fixed set of admin-editable display labels (e.g. what to call an "Academic Year" for
 * this organization) -- read by every authenticated user (via the terminology hook), written only
 * by an admin. Deliberately edit-only: rows are seeded by migration and there is no create/delete
 * here, since the frontend only ever reads by a known, hardcoded key -- letting an admin invent
 * arbitrary new keys would just create rows nothing reads.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationSettingService {

    private final OrganizationSettingRepository settingRepository;

    @Transactional(readOnly = true)
    public List<OrganizationSetting> getAll() {
        return settingRepository.findAll();
    }

    public OrganizationSetting updateValue(String settingKey, String value) {
        OrganizationSetting setting = settingRepository.findBySettingKey(settingKey)
                .orElseThrow(() -> new ResourceNotFoundException("OrganizationSetting not found with key: " + settingKey));
        setting.setValue(value);
        return settingRepository.save(setting);
    }
}
