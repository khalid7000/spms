package com.rit.spms.service;

import com.rit.spms.domain.*;
import com.rit.spms.dto.request.CreateMeasurementRequest;
import com.rit.spms.dto.response.MeasurementResponse;
import com.rit.spms.exception.ResourceNotFoundException;
import com.rit.spms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MeasurementService {

    private final MeasurementRepository measurementRepository;
    private final InitiativeRepository initiativeRepository;
    private final AppUserRepository appUserRepository;
    private final PermissionService permissionService;
    private final AuditService auditService;

    public Measurement createMeasurement(Long initiativeId, CreateMeasurementRequest req, Long currentUserId) {
        Initiative initiative = initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Initiative", initiativeId));

        Long strategyId = initiative.getObjective().getGoal().getStrategy().getId();
        permissionService.assertCanEditContent(currentUserId, strategyId);

        Measurement measurement = Measurement.builder()
                .initiative(initiative)
                .description(req.getDescription())
                .unit(req.getUnit())
                .targetValue(req.getTargetValue())
                .actualValue(req.getActualValue())
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0)
                .build();
        measurement = measurementRepository.save(measurement);

        AppUser user = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        auditService.log(user, "CREATE_MEASUREMENT", "Measurement", measurement.getId(),
                initiative.getObjective().getGoal().getStrategy(),
                "Created measurement: " + measurement.getDescription());
        return measurement;
    }

    public Measurement updateMeasurement(Long measurementId, CreateMeasurementRequest req, Long currentUserId) {
        Measurement measurement = measurementRepository.findById(measurementId)
                .orElseThrow(() -> new ResourceNotFoundException("Measurement", measurementId));

        Strategy strategy = measurement.getInitiative().getObjective().getGoal().getStrategy();
        permissionService.assertCanEditContent(currentUserId, strategy.getId());

        String oldDesc = measurement.getDescription();
        measurement.setDescription(req.getDescription());
        if (req.getUnit() != null) measurement.setUnit(req.getUnit());
        if (req.getTargetValue() != null) measurement.setTargetValue(req.getTargetValue());
        if (req.getActualValue() != null) measurement.setActualValue(req.getActualValue());
        if (req.getSortOrder() != null) measurement.setSortOrder(req.getSortOrder());
        measurement = measurementRepository.save(measurement);

        AppUser user = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        auditService.log(user, "UPDATE_MEASUREMENT", "Measurement", measurementId, strategy,
                oldDesc, measurement.getDescription(), "Updated measurement");
        return measurement;
    }

    public void deleteMeasurement(Long measurementId, Long currentUserId) {
        Measurement measurement = measurementRepository.findById(measurementId)
                .orElseThrow(() -> new ResourceNotFoundException("Measurement", measurementId));

        Strategy strategy = measurement.getInitiative().getObjective().getGoal().getStrategy();
        permissionService.assertCanEditContent(currentUserId, strategy.getId());

        AppUser user = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", currentUserId));
        auditService.log(user, "DELETE_MEASUREMENT", "Measurement", measurementId, strategy,
                "Deleted measurement: " + measurement.getDescription());
        measurementRepository.delete(measurement);
    }

    @Transactional(readOnly = true)
    public List<MeasurementResponse> getMeasurements(Long initiativeId, Long currentUserId) {
        Initiative initiative = initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Initiative", initiativeId));
        permissionService.assertCanRead(currentUserId, initiative.getObjective().getGoal().getStrategy().getId());

        return measurementRepository.findByInitiativeIdOrderBySortOrder(initiativeId)
                .stream().map(this::toResponse).toList();
    }

    public MeasurementResponse toResponse(Measurement m) {
        return MeasurementResponse.builder()
                .id(m.getId())
                .initiativeId(m.getInitiative().getId())
                .description(m.getDescription())
                .unit(m.getUnit())
                .targetValue(m.getTargetValue())
                .actualValue(m.getActualValue())
                .sortOrder(m.getSortOrder())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
