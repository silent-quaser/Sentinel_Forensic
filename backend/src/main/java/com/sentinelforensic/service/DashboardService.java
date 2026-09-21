package com.sentinelforensic.service;

import com.sentinelforensic.dto.ActivityPointDto;
import com.sentinelforensic.dto.DashboardSummaryDto;
import com.sentinelforensic.dto.InvestigationResponse;
import com.sentinelforensic.dto.ThreatDto;
import com.sentinelforensic.model.Investigation;
import com.sentinelforensic.model.InvestigationStatus;
import com.sentinelforensic.model.LogEntry;
import com.sentinelforensic.model.Threat;
import com.sentinelforensic.model.ThreatSeverity;
import com.sentinelforensic.repository.InvestigationRepository;
import com.sentinelforensic.repository.LogEntryRepository;
import com.sentinelforensic.repository.ThreatRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final InvestigationRepository investigationRepository;
    private final LogEntryRepository logEntryRepository;
    private final ThreatRepository threatRepository;
    private final InvestigationService investigationService;
    private final ThreatService threatService;

    public DashboardService(
            InvestigationRepository investigationRepository,
            LogEntryRepository logEntryRepository,
            ThreatRepository threatRepository,
            InvestigationService investigationService,
            ThreatService threatService) {
        this.investigationRepository = investigationRepository;
        this.logEntryRepository = logEntryRepository;
        this.threatRepository = threatRepository;
        this.investigationService = investigationService;
        this.threatService = threatService;
    }

    public DashboardSummaryDto getDashboardSummary() {
        DashboardSummaryDto summary = new DashboardSummaryDto();

        // 1. Database-backed top summary cards
        long activeInvestigations = investigationRepository.countByStatus(InvestigationStatus.IN_PROGRESS);
        long totalEvents = logEntryRepository.count();
        long totalThreats = threatRepository.count();
        long highCriticalThreats = threatRepository.countBySeverityIn(List.of(ThreatSeverity.HIGH, ThreatSeverity.CRITICAL));
        long distinctUsers = logEntryRepository.countDistinctUsersGlobal();

        summary.setActiveInvestigations(activeInvestigations);
        summary.setTotalEvents(totalEvents);
        summary.setDetectedThreats(totalThreats);
        summary.setHighCriticalThreats(highCriticalThreats);
        summary.setDistinctUsers(distinctUsers);

        // 2. Threat Severity Distribution from actual database threats
        Map<String, Long> severityDist = new LinkedHashMap<>();
        severityDist.put("CRITICAL", threatRepository.countBySeverity(ThreatSeverity.CRITICAL));
        severityDist.put("HIGH", threatRepository.countBySeverity(ThreatSeverity.HIGH));
        severityDist.put("MEDIUM", threatRepository.countBySeverity(ThreatSeverity.MEDIUM));
        severityDist.put("LOW", threatRepository.countBySeverity(ThreatSeverity.LOW));
        summary.setThreatSeverityDistribution(severityDist);

        // 3. Event Type Distribution from actual database log entries
        List<Object[]> eventTypeCounts = logEntryRepository.countByEventTypeGlobal();
        Map<String, Long> eventTypeDist = new LinkedHashMap<>();
        for (Object[] row : eventTypeCounts) {
            if (row[0] != null) {
                eventTypeDist.put(row[0].toString(), (Long) row[1]);
            }
        }
        summary.setEventTypeDistribution(eventTypeDist);

        // 4. Recent Investigations from database
        List<Investigation> recentInvs = investigationRepository.findAllByOrderByCreatedAtDesc();
        List<InvestigationResponse> invResponses = recentInvs.stream()
                .limit(5)
                .map(investigationService::toResponseDto)
                .collect(Collectors.toList());
        summary.setRecentInvestigations(invResponses);

        // 5. Recent Threats from database
        List<Threat> recentThreats = threatRepository.findAllByOrderByDetectedAtDesc();
        List<ThreatDto> threatDtos = recentThreats.stream()
                .limit(5)
                .map(threatService::toThreatDtoWithoutEvidence)
                .collect(Collectors.toList());
        summary.setRecentThreats(threatDtos);

        // 6. Activity Series (Events & Threats over time from database)
        summary.setActivitySeries(buildActivitySeries());

        return summary;
    }

    private List<ActivityPointDto> buildActivitySeries() {
        List<ActivityPointDto> series = new ArrayList<>();
        List<LogEntry> allLogs = logEntryRepository.findAll();
        List<Threat> allThreats = threatRepository.findAll();

        if (allLogs.isEmpty()) {
            return series;
        }

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM dd");

        // Group logs by date
        Map<String, Long> eventsByDate = allLogs.stream()
                .filter(l -> l.getTimestamp() != null)
                .collect(Collectors.groupingBy(l -> l.getTimestamp().format(dateFmt), LinkedHashMap::new, Collectors.counting()));

        // Group threats by date
        Map<String, Long> threatsByDate = allThreats.stream()
                .filter(t -> t.getDetectedAt() != null)
                .collect(Collectors.groupingBy(t -> t.getDetectedAt().format(dateFmt), LinkedHashMap::new, Collectors.counting()));

        // Combine keys in chronological order
        Set<String> dates = new TreeSet<>(eventsByDate.keySet());
        dates.addAll(threatsByDate.keySet());

        for (String d : dates) {
            long eCount = eventsByDate.getOrDefault(d, 0L);
            long tCount = threatsByDate.getOrDefault(d, 0L);
            series.add(new ActivityPointDto(d, eCount, tCount));
        }

        return series;
    }
}
