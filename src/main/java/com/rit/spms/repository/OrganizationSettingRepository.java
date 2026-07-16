package com.rit.spms.repository;

import com.rit.spms.domain.OrganizationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationSettingRepository extends JpaRepository<OrganizationSetting, Long> {
    Optional<OrganizationSetting> findBySettingKey(String settingKey);
}
