package com.rit.spms.platform.repository;

import com.rit.spms.platform.domain.Organization;
import com.rit.spms.platform.domain.enums.OrgStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySchemaName(String schemaName);
    Optional<Organization> findByIsDefaultTrue();
    List<Organization> findByStatus(OrgStatus status);
}
