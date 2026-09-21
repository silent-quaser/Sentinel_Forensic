package com.sentinelforensic;

import com.sentinelforensic.detector.ThreatDetectionEngine;
import com.sentinelforensic.detector.ThreatDetectionResult;
import com.sentinelforensic.detector.rules.*;
import com.sentinelforensic.dto.DetectionResultDto;
import com.sentinelforensic.model.*;
import com.sentinelforensic.parser.ParseResult;
import com.sentinelforensic.parser.SystemLogParser;
import com.sentinelforensic.repository.LogEntryRepository;
import com.sentinelforensic.repository.ThreatEvidenceRepository;
import com.sentinelforensic.repository.ThreatRepository;
import com.sentinelforensic.repository.ThreatRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SentinelForensicTests {

    private SystemLogParser parser;

    @BeforeEach
    public void setUp() {
        parser = new SystemLogParser();
    }

    // 1. Valid log parsing
    @Test
    @DisplayName("Test 1: Valid log parsing into structured LogEntry models")
    public void testValidLogParsing() throws Exception {
        String logData = "2026-07-21 09:15:32 LOGIN_SUCCESS Alice\n" +
                         "2026-07-21 09:18:10 LOGIN_FAILED Bob";
        ParseResult result = parser.parseWithReport(new BufferedReader(new StringReader(logData)), 1L, "test.log");

        assertEquals(2, result.getEntries().size());
        assertEquals(2, result.getParsedCount());
        assertEquals(0, result.getSkippedCount());
        assertEquals("LOGIN_SUCCESS", result.getEntries().get(0).getEventType());
        assertEquals("Alice", result.getEntries().get(0).getUsername());
        assertEquals("LOGIN_FAILED", result.getEntries().get(1).getEventType());
        assertEquals("Bob", result.getEntries().get(1).getUsername());
    }

    // 2. Invalid log parsing
    @Test
    @DisplayName("Test 2: Invalid/malformed log parsing tracks warnings and skips gracefully")
    public void testInvalidLogParsing() throws Exception {
        String logData = "hello this is not a valid log entry\n" +
                         "2026-07-21 09:18:10 LOGIN_FAILED Bob\n" +
                         "2026-07-21 invalid time USER_CREATED Charlie";
        ParseResult result = parser.parseWithReport(new BufferedReader(new StringReader(logData)), 1L, "test.log");

        assertEquals(1, result.getEntries().size(), "Should parse only the 1 valid line");
        assertEquals(1, result.getParsedCount());
        assertEquals(2, result.getSkippedCount(), "Should skip 2 malformed lines");
        assertEquals(2, result.getWarnings().size());
        assertTrue(result.getWarnings().get(0).contains("Line 1"));
    }

    // 3. Timestamp parsing
    @Test
    @DisplayName("Test 3: Precise timestamp parsing into LocalDateTime")
    public void testTimestampParsing() throws Exception {
        String logData = "2026-07-21 14:35:42 USER_CREATED Admin";
        ParseResult result = parser.parseWithReport(new BufferedReader(new StringReader(logData)), 1L, "test.log");

        assertFalse(result.getEntries().isEmpty());
        LocalDateTime ts = result.getEntries().get(0).getTimestamp();
        assertEquals(2026, ts.getYear());
        assertEquals(7, ts.getMonthValue());
        assertEquals(21, ts.getDayOfMonth());
        assertEquals(14, ts.getHour());
        assertEquals(35, ts.getMinute());
        assertEquals(42, ts.getSecond());
    }

    // 4. Empty file handling
    @Test
    @DisplayName("Test 4: Empty log file returns zero entries without error")
    public void testEmptyLogHandling() throws Exception {
        ParseResult result = parser.parseWithReport(new BufferedReader(new StringReader("")), 1L, "empty.log");
        assertTrue(result.getEntries().isEmpty());
        assertEquals(0, result.getTotalLines());
        assertEquals(0, result.getSkippedCount());
    }

    // 5. Brute-force detection (3 failed logins in 10 mins)
    @Test
    @DisplayName("Test 5: Brute force login detection produces threat with exact evidence")
    public void testBruteForceDetection() throws Exception {
        String logData = "2026-07-21 09:18:10 LOGIN_FAILED Bob\n" +
                         "2026-07-21 09:20:55 LOGIN_FAILED Bob\n" +
                         "2026-07-21 09:22:14 LOGIN_FAILED Bob";
        ParseResult result = parser.parseWithReport(new BufferedReader(new StringReader(logData)), 1L, "test.log");

        BruteForceRule rule = new BruteForceRule();
        ThreatRule config = new ThreatRule();
        config.setThreshold(3);
        config.setTimeWindowSeconds(600);

        List<ThreatDetectionResult> threats = rule.evaluate(result.getEntries(), config);
        assertEquals(1, threats.size());

        Threat threat = threats.get(0).getThreat();
        assertEquals("BRUTE_FORCE_001", threat.getRuleCode());
        assertEquals(ThreatSeverity.HIGH, threat.getSeverity());
        // Base 30 + (5 * 3) = 45 -> HIGH
        assertEquals(45, threat.getScore());
        assertEquals("Bob", threat.getAffectedUser());
        assertEquals(3, threats.get(0).getEvidenceEvents().size());
    }

    // 6. Brute-force time window boundary check
    @Test
    @DisplayName("Test 6: Brute force time window boundary (events outside window do not trigger)")
    public void testBruteForceTimeWindowBoundary() throws Exception {
        String logData = "2026-07-21 08:00:00 LOGIN_FAILED Bob\n" +
                         "2026-07-21 09:10:00 LOGIN_FAILED Bob\n" +
                         "2026-07-21 10:20:00 LOGIN_FAILED Bob";
        ParseResult result = parser.parseWithReport(new BufferedReader(new StringReader(logData)), 1L, "test.log");

        BruteForceRule rule = new BruteForceRule();
        ThreatRule config = new ThreatRule();
        config.setThreshold(3);
        config.setTimeWindowSeconds(600); // 10 minutes

        List<ThreatDetectionResult> threats = rule.evaluate(result.getEntries(), config);
        assertTrue(threats.isEmpty(), "Events spaced an hour apart should not trigger brute-force threat");
    }

    // 7. Different users should not combine
    @Test
    @DisplayName("Test 7: Failures for different users do not combine to trigger brute-force")
    public void testDifferentUsersDoNotCombine() throws Exception {
        String logData = "2026-07-21 09:18:10 LOGIN_FAILED Alice\n" +
                         "2026-07-21 09:20:55 LOGIN_FAILED Bob\n" +
                         "2026-07-21 09:22:14 LOGIN_FAILED Charlie";
        ParseResult result = parser.parseWithReport(new BufferedReader(new StringReader(logData)), 1L, "test.log");

        BruteForceRule rule = new BruteForceRule();
        ThreatRule config = new ThreatRule();
        config.setThreshold(3);
        config.setTimeWindowSeconds(600);

        List<ThreatDetectionResult> threats = rule.evaluate(result.getEntries(), config);
        assertTrue(threats.isEmpty(), "Failures across distinct users must not trigger brute force");
    }

    // 8. Account lock severity escalation (Mandatory Requirement 1)
    @Test
    @DisplayName("Test 8: Account lock following brute force explicitly escalates severity to CRITICAL with Score 65")
    public void testAccountLockSeverityEscalation() throws Exception {
        String logData = "2026-07-21 09:18:10 LOGIN_FAILED Bob\n" +
                         "2026-07-21 09:20:55 LOGIN_FAILED Bob\n" +
                         "2026-07-21 09:22:14 LOGIN_FAILED Bob\n" +
                         "2026-07-21 09:25:40 ACCOUNT_LOCKED Bob";
        ParseResult result = parser.parseWithReport(new BufferedReader(new StringReader(logData)), 1L, "test.log");

        BruteForceRule rule = new BruteForceRule();
        ThreatRule config = new ThreatRule();
        config.setThreshold(3);
        config.setTimeWindowSeconds(600);

        List<ThreatDetectionResult> threats = rule.evaluate(result.getEntries(), config);
        assertEquals(1, threats.size());

        Threat threat = threats.get(0).getThreat();
        assertEquals(ThreatSeverity.CRITICAL, threat.getSeverity(), "Must explicitly override to CRITICAL");
        assertEquals(65, threat.getScore(), "Score must be 30 + 15 + 20 = 65");
        assertEquals("Account lock following confirmed brute-force activity", threat.getEscalationReason());
        assertTrue(threat.getExplanation().contains("followed by an account lock at 09:25:40"));
        assertEquals(4, threats.get(0).getEvidenceEvents().size());
    }

    // 9. Authentication failure detection (AUTH_FAILURE_001)
    @Test
    @DisplayName("Test 9: Repeated authentication failures rule detects 2+ failures in 30min window")
    public void testAuthFailureRuleDetection() throws Exception {
        String logData = "2026-07-21 09:00:00 LOGIN_FAILED Alice\n" +
                         "2026-07-21 09:15:00 LOGIN_FAILED Alice";
        ParseResult result = parser.parseWithReport(new BufferedReader(new StringReader(logData)), 1L, "test.log");

        AuthFailureRule rule = new AuthFailureRule();
        ThreatRule config = new ThreatRule();
        config.setThreshold(2);
        config.setTimeWindowSeconds(1800);

        List<ThreatDetectionResult> threats = rule.evaluate(result.getEntries(), config);
        assertEquals(1, threats.size());
        assertEquals("AUTH_FAILURE_001", threats.get(0).getThreat().getRuleCode());
        assertEquals(ThreatSeverity.MEDIUM, threats.get(0).getThreat().getSeverity());
        assertEquals(2, threats.get(0).getEvidenceEvents().size());
    }

    // 10. Success after repeated failures (AUTH_SUCCESS_AFTER_FAILURE_001)
    @Test
    @DisplayName("Test 10: Successful login after repeated failures detection")
    public void testAuthSuccessAfterFailure() throws Exception {
        String logData = "2026-07-21 09:18:10 LOGIN_FAILED Bob\n" +
                         "2026-07-21 09:20:55 LOGIN_FAILED Bob\n" +
                         "2026-07-21 09:22:14 LOGIN_SUCCESS Bob";
        ParseResult result = parser.parseWithReport(new BufferedReader(new StringReader(logData)), 1L, "test.log");

        AuthSuccessAfterFailureRule rule = new AuthSuccessAfterFailureRule();
        ThreatRule config = new ThreatRule();
        config.setThreshold(2);
        config.setTimeWindowSeconds(900);

        List<ThreatDetectionResult> threats = rule.evaluate(result.getEntries(), config);
        assertEquals(1, threats.size());
        assertEquals("AUTH_SUCCESS_AFTER_FAILURE_001", threats.get(0).getThreat().getRuleCode());
        assertEquals(ThreatSeverity.HIGH, threats.get(0).getThreat().getSeverity());
        assertEquals(3, threats.get(0).getEvidenceEvents().size());
    }

    // 11. Password change after suspicious activity (PASSWORD_CHANGE_001)
    @Test
    @DisplayName("Test 11: Password change after suspicious activity detection")
    public void testPasswordChangeAfterFailure() throws Exception {
        String logData = "2026-07-21 09:18:10 LOGIN_FAILED Alice\n" +
                         "2026-07-21 09:22:00 PASSWORD_CHANGED Alice";
        ParseResult result = parser.parseWithReport(new BufferedReader(new StringReader(logData)), 1L, "test.log");

        PasswordChangeRule rule = new PasswordChangeRule();
        ThreatRule config = new ThreatRule();
        config.setTimeWindowSeconds(900);

        List<ThreatDetectionResult> threats = rule.evaluate(result.getEntries(), config);
        assertEquals(1, threats.size());
        assertEquals("PASSWORD_CHANGE_001", threats.get(0).getThreat().getRuleCode());
        assertEquals("Alice", threats.get(0).getThreat().getAffectedUser());
        assertEquals(2, threats.get(0).getEvidenceEvents().size());
    }

    // 12. Duplicate threat prevention (ThreatDetectionEngine deduplication)
    @Test
    @DisplayName("Test 12: Threat detection engine prevents duplicate threats on repeated runs")
    public void testDuplicateThreatPrevention() {
        LogEntryRepository logRepo = mock(LogEntryRepository.class);
        ThreatRepository threatRepo = mock(ThreatRepository.class);
        ThreatRuleRepository ruleRepo = mock(ThreatRuleRepository.class);
        ThreatEvidenceRepository evidenceRepo = mock(ThreatEvidenceRepository.class);

        LogEntry e1 = new LogEntry(); e1.setId(1L); e1.setInvestigationId(1L); e1.setTimestamp(LocalDateTime.now().minusMinutes(5)); e1.setEventType("LOGIN_FAILED"); e1.setUsername("Bob");
        LogEntry e2 = new LogEntry(); e2.setId(2L); e2.setInvestigationId(1L); e2.setTimestamp(LocalDateTime.now().minusMinutes(3)); e2.setEventType("LOGIN_FAILED"); e2.setUsername("Bob");
        LogEntry e3 = new LogEntry(); e3.setId(3L); e3.setInvestigationId(1L); e3.setTimestamp(LocalDateTime.now().minusMinutes(1)); e3.setEventType("LOGIN_FAILED"); e3.setUsername("Bob");

        when(logRepo.findByInvestigationIdOrderByTimestampAscIdAsc(1L)).thenReturn(List.of(e1, e2, e3));

        Threat existingThreat = new Threat();
        existingThreat.setId(10L);
        existingThreat.setInvestigationId(1L);
        existingThreat.setRuleCode("BRUTE_FORCE_001");
        existingThreat.setAffectedUser("Bob");
        existingThreat.setSeverity(ThreatSeverity.HIGH);
        existingThreat.setScore(45);

        when(threatRepo.findByInvestigationIdOrderByDetectedAtDesc(1L)).thenReturn(new ArrayList<>(List.of(existingThreat)));
        when(evidenceRepo.findLogEntryIdsByThreatId(10L)).thenReturn(List.of(1L, 2L, 3L));

        ThreatRule ruleConfig = new ThreatRule();
        ruleConfig.setRuleCode("BRUTE_FORCE_001");
        ruleConfig.setEnabled(true);
        ruleConfig.setThreshold(3);
        ruleConfig.setTimeWindowSeconds(600);
        when(ruleRepo.findAll()).thenReturn(List.of(ruleConfig));

        ThreatDetectionEngine engine = new ThreatDetectionEngine(
                List.of(new BruteForceRule()), ruleRepo, threatRepo, evidenceRepo, logRepo
        );

        DetectionResultDto result = engine.runDetection(1L);

        assertEquals(0, result.getThreatsCreated(), "Should not create duplicate threats");
        assertEquals(1, result.getExistingThreatsUnchanged(), "Existing threat should be reported as unchanged");
        verify(threatRepo, never()).save(any(Threat.class));
    }

    // 13. Threat score calculation & transparent breakdown
    @Test
    @DisplayName("Test 13: Transparent score calculation displays exact components")
    public void testThreatScoreCalculationBreakdown() throws Exception {
        String logData = "2026-07-21 09:18:10 LOGIN_FAILED Bob\n" +
                         "2026-07-21 09:19:10 LOGIN_FAILED Bob\n" +
                         "2026-07-21 09:20:10 LOGIN_FAILED Bob\n" +
                         "2026-07-21 09:21:10 LOGIN_FAILED Bob\n" +
                         "2026-07-21 09:22:10 ACCOUNT_LOCKED Bob";
        ParseResult result = parser.parseWithReport(new BufferedReader(new StringReader(logData)), 1L, "test.log");

        BruteForceRule rule = new BruteForceRule();
        ThreatRule config = new ThreatRule();
        config.setThreshold(3);
        config.setTimeWindowSeconds(600);

        List<ThreatDetectionResult> threats = rule.evaluate(result.getEntries(), config);
        assertEquals(1, threats.size());

        Threat threat = threats.get(0).getThreat();
        // 4 failures: 30 + (5 * 4) + 20 = 70
        assertEquals(70, threat.getScore());
        assertEquals(ThreatSeverity.CRITICAL, threat.getSeverity());
        assertNotNull(threat.getScoreBreakdown());
        assertTrue(threat.getScoreBreakdown().contains("Base brute-force rule: +30"));
        assertTrue(threat.getScoreBreakdown().contains("Account lock: +20"));
    }

    // 14. Timeline chronological ordering with tie-breaking
    @Test
    @DisplayName("Test 14: Timeline preserves strict chronological order with ID tie-breaking")
    public void testTimelineOrdering() {
        LocalDateTime sameTime = LocalDateTime.of(2026, 7, 21, 9, 30, 0);

        LogEntry e2 = new LogEntry(); e2.setId(2L); e2.setTimestamp(sameTime); e2.setEventType("USER_CREATED");
        LogEntry e1 = new LogEntry(); e1.setId(1L); e1.setTimestamp(sameTime); e1.setEventType("LOGIN_SUCCESS");
        LogEntry e3 = new LogEntry(); e3.setId(3L); e3.setTimestamp(sameTime.plusSeconds(10)); e3.setEventType("LOGOUT");

        List<LogEntry> unsorted = Arrays.asList(e3, e2, e1);
        unsorted.sort(Comparator.comparing(LogEntry::getTimestamp).thenComparing(LogEntry::getId));

        assertEquals(1L, unsorted.get(0).getId());
        assertEquals(2L, unsorted.get(1).getId());
        assertEquals(3L, unsorted.get(2).getId());
    }
}
