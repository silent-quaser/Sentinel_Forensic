package com.sentinelforensic.service;

import com.sentinelforensic.dto.LogEntryDto;
import com.sentinelforensic.dto.LogUploadResponse;
import com.sentinelforensic.dto.TimelineEventDto;
import com.sentinelforensic.exception.InvalidLogException;
import com.sentinelforensic.exception.ResourceNotFoundException;
import com.sentinelforensic.model.Investigation;
import com.sentinelforensic.model.LogEntry;
import com.sentinelforensic.model.Threat;
import com.sentinelforensic.parser.ParseResult;
import com.sentinelforensic.parser.SystemLogParser;
import com.sentinelforensic.repository.InvestigationRepository;
import com.sentinelforensic.repository.LogEntryRepository;
import com.sentinelforensic.repository.ThreatEvidenceRepository;
import com.sentinelforensic.repository.ThreatRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LogService {
    private static final Logger log = LoggerFactory.getLogger(LogService.class);

    private final LogEntryRepository logEntryRepository;
    private final InvestigationRepository investigationRepository;
    private final ThreatEvidenceRepository threatEvidenceRepository;
    private final ThreatRepository threatRepository;
    private final SystemLogParser systemLogParser;

    public LogService(
            LogEntryRepository logEntryRepository,
            InvestigationRepository investigationRepository,
            ThreatEvidenceRepository threatEvidenceRepository,
            ThreatRepository threatRepository,
            SystemLogParser systemLogParser) {
        this.logEntryRepository = logEntryRepository;
        this.investigationRepository = investigationRepository;
        this.threatEvidenceRepository = threatEvidenceRepository;
        this.threatRepository = threatRepository;
        this.systemLogParser = systemLogParser;
    }

    @Transactional
    public LogUploadResponse uploadLogFile(Long investigationId, MultipartFile file) {
        Investigation inv = investigationRepository.findById(investigationId)
                .orElseThrow(() -> new ResourceNotFoundException("Investigation", "id", investigationId));

        if (file == null || file.isEmpty()) {
            throw new InvalidLogException("Uploaded log file is empty.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "uploaded_log.log";
        }

        log.info("Processing log upload: '{}' for investigation {} ({})", originalFilename, investigationId, inv.getName());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            ParseResult result = systemLogParser.parseWithReport(reader, investigationId, originalFilename);

            if (!result.getEntries().isEmpty()) {
                logEntryRepository.saveAll(result.getEntries());
                log.info("Successfully persisted {} log entries for investigation {}", result.getEntries().size(), investigationId);
            }

            LogUploadResponse response = new LogUploadResponse(
                originalFilename,
                result.getTotalLines(),
                result.getParsedCount(),
                result.getSkippedCount()
            );
            response.setWarnings(result.getWarnings());
            response.setErrors(result.getErrors());

            return response;
        } catch (IOException e) {
            log.error("Failed to read uploaded log file", e);
            throw new InvalidLogException("Failed to read log file: " + e.getMessage(), e);
        }
    }

    public Page<LogEntryDto> getInvestigationLogs(
            Long investigationId,
            String eventType,
            String username,
            String source,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String keyword,
            Pageable pageable) {

        if (!investigationRepository.existsById(investigationId)) {
            throw new ResourceNotFoundException("Investigation", "id", investigationId);
        }

        Specification<LogEntry> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("investigationId"), investigationId));

            if (eventType != null && !eventType.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("eventType"), eventType.trim()));
            }
            if (username != null && !username.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("username")), "%" + username.trim().toLowerCase() + "%"));
            }
            if (source != null && !source.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("source")), "%" + source.trim().toLowerCase() + "%"));
            }
            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), startTime));
            }
            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), endTime));
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim().toLowerCase() + "%";
                Predicate rawMatch = cb.like(cb.lower(root.get("rawMessage")), kw);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), kw);
                predicates.add(cb.or(rawMatch, descMatch));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<LogEntry> page = logEntryRepository.findAll(spec, pageable);
        List<LogEntryDto> dtos = page.getContent().stream()
                .map(this::toLogEntryDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    public List<TimelineEventDto> getTimeline(Long investigationId) {
        if (!investigationRepository.existsById(investigationId)) {
            throw new ResourceNotFoundException("Investigation", "id", investigationId);
        }

        List<LogEntry> logs = logEntryRepository.findByInvestigationIdOrderByTimestampAscIdAsc(investigationId);

        Map<Long, Threat> threatMap = threatRepository.findByInvestigationIdOrderByDetectedAtDesc(investigationId)
                .stream().collect(Collectors.toMap(Threat::getId, t -> t, (a, b) -> a));

        List<TimelineEventDto> timeline = new ArrayList<>();
        for (LogEntry entry : logs) {
            TimelineEventDto dto = new TimelineEventDto();
            dto.setId(entry.getId());
            dto.setInvestigationId(entry.getInvestigationId());
            dto.setTimestamp(entry.getTimestamp());
            dto.setEventType(entry.getEventType());
            dto.setUsername(entry.getUsername());
            dto.setSource(entry.getSource());
            dto.setSeverity(entry.getSeverity());
            dto.setDescription(entry.getDescription());
            dto.setRawMessage(entry.getRawMessage());

            List<Long> threatIds = threatEvidenceRepository.findThreatIdsByLogEntryId(entry.getId());
            boolean isSuspicious = "LOGIN_FAILED".equalsIgnoreCase(entry.getEventType())
                    || "ACCOUNT_LOCKED".equalsIgnoreCase(entry.getEventType())
                    || !threatIds.isEmpty();

            dto.setSuspicious(isSuspicious);

            for (Long tid : threatIds) {
                Threat t = threatMap.get(tid);
                if (t != null) {
                    dto.getAssociatedThreatIds().add(t.getId());
                    dto.getAssociatedThreatTitles().add(t.getTitle() + " (" + t.getSeverity() + ")");
                }
            }

            timeline.add(dto);
        }

        return timeline;
    }

    public Page<LogEntryDto> searchGlobalLogs(String eventType, String username, String source, String keyword, Pageable pageable) {
        Specification<LogEntry> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (eventType != null && !eventType.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("eventType"), eventType.trim()));
            }
            if (username != null && !username.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("username")), "%" + username.trim().toLowerCase() + "%"));
            }
            if (source != null && !source.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("source")), "%" + source.trim().toLowerCase() + "%"));
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim().toLowerCase() + "%";
                Predicate rawMatch = cb.like(cb.lower(root.get("rawMessage")), kw);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), kw);
                predicates.add(cb.or(rawMatch, descMatch));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<LogEntry> page = logEntryRepository.findAll(spec, pageable);
        List<LogEntryDto> dtos = page.getContent().stream().map(this::toLogEntryDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    public LogEntryDto toLogEntryDto(LogEntry entry) {
        LogEntryDto dto = new LogEntryDto();
        dto.setId(entry.getId());
        dto.setInvestigationId(entry.getInvestigationId());
        dto.setTimestamp(entry.getTimestamp());
        dto.setEventType(entry.getEventType());
        dto.setUsername(entry.getUsername());
        dto.setSource(entry.getSource());
        dto.setSourceType(entry.getSourceType());
        dto.setIpAddress(entry.getIpAddress());
        dto.setHostname(entry.getHostname());
        dto.setProcessName(entry.getProcessName());
        dto.setProcessId(entry.getProcessId());
        dto.setSeverity(entry.getSeverity());
        dto.setDescription(entry.getDescription());
        dto.setRawMessage(entry.getRawMessage());

        List<Long> threatIds = threatEvidenceRepository.findThreatIdsByLogEntryId(entry.getId());
        dto.setReferencedThreatIds(threatIds);

        return dto;
    }
}