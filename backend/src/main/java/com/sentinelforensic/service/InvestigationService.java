package com.sentinelforensic.service;

import com.sentinelforensic.dto.CreateInvestigationRequest;
import com.sentinelforensic.dto.InvestigationResponse;
import com.sentinelforensic.exception.ResourceNotFoundException;
import com.sentinelforensic.model.Investigation;
import com.sentinelforensic.model.InvestigationStatus;
import com.sentinelforensic.model.ThreatSeverity;
import com.sentinelforensic.repository.InvestigationRepository;
import com.sentinelforensic.repository.LogEntryRepository;
import com.sentinelforensic.repository.ThreatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class InvestigationService {
    private static final Logger log = LoggerFactory.getLogger(InvestigationService.class);
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(1);

    private final InvestigationRepository investigationRepository;
    private final LogEntryRepository logEntryRepository;
    private final ThreatRepository threatRepository;

    public InvestigationService(
            InvestigationRepository investigationRepository,
            LogEntryRepository logEntryRepository,
            ThreatRepository threatRepository) {
        this.investigationRepository = investigationRepository;
        this.logEntryRepository = logEntryRepository;
        this.threatRepository = threatRepository;
    }

    public List<InvestigationResponse> getAllInvestigations(String query) {
        List<Investigation> list;
        if (query != null && !query.isBlank()) {
            list = investigationRepository.searchInvestigations(query.trim());
        } else {
            list = investigationRepository.findAllByOrderByCreatedAtDesc();
        }

        return list.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public InvestigationResponse getInvestigationById(Long id) {
        Investigation inv = investigationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investigation", "id", id));
        return toResponseDto(inv);
    }

    public Investigation getEntityById(Long id) {
        return investigationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investigation", "id", id));
    }

    @Transactional
    public InvestigationResponse createInvestigation(CreateInvestigationRequest request) {
        log.info("Creating new investigation: '{}' by '{}'", request.getName(), request.getInvestigatorName());

        Investigation inv = new Investigation();
        inv.setName(request.getName());
        inv.setInvestigatorName(request.getInvestigatorName());
        inv.setDescription(request.getDescription());
        inv.setStatus(InvestigationStatus.IN_PROGRESS);

        // Deterministic, human-readable investigation code: INV-2026-001
        String year = String.valueOf(LocalDateTime.now().getYear());
        long count = investigationRepository.count() + 1;
        String formattedCode = String.format("INV-%s-%03d", year, count);
        inv.setInvestigationId(formattedCode);

        Investigation saved = investigationRepository.save(inv);
        return toResponseDto(saved);
    }

    @Transactional
    public InvestigationResponse updateStatus(Long id, InvestigationStatus newStatus) {
        Investigation inv = getEntityById(id);
        inv.setStatus(newStatus);
        inv.setUpdatedAt(LocalDateTime.now());
        Investigation updated = investigationRepository.save(inv);
        log.info("Updated investigation '{}' status to {}", id, newStatus);
        return toResponseDto(updated);
    }

    @Transactional
    public void deleteInvestigation(Long id) {
        Investigation inv = getEntityById(id);
        log.info("Deleting investigation {} ('{}')", id, inv.getName());
        investigationRepository.delete(inv);
    }

    public InvestigationResponse toResponseDto(Investigation inv) {
        InvestigationResponse dto = new InvestigationResponse();
        dto.setId(inv.getId());
        dto.setInvestigationId(inv.getInvestigationId());
        dto.setName(inv.getName());
        dto.setInvestigatorName(inv.getInvestigatorName());
        dto.setDescription(inv.getDescription());
        dto.setStatus(inv.getStatus());
        dto.setCreatedAt(inv.getCreatedAt());
        dto.setUpdatedAt(inv.getUpdatedAt());

        long eventCount = logEntryRepository.countByInvestigationId(inv.getId());
        long threatCount = threatRepository.countByInvestigationId(inv.getId());
        long criticalCount = threatRepository.countByInvestigationIdAndSeverity(inv.getId(), ThreatSeverity.CRITICAL);
        long highCount = threatRepository.countByInvestigationIdAndSeverity(inv.getId(), ThreatSeverity.HIGH);

        dto.setEventCount(eventCount);
        dto.setThreatCount(threatCount);
        dto.setCriticalThreatCount(criticalCount);
        dto.setHighThreatCount(highCount);

        return dto;
    }
}