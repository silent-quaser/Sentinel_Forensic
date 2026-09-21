package com.sentinelforensic.service;

import com.sentinelforensic.exception.ResourceNotFoundException;
import com.sentinelforensic.model.*;
import com.sentinelforensic.repository.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReportService {
    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final InvestigationRepository investigationRepository;
    private final LogEntryRepository logEntryRepository;
    private final ThreatRepository threatRepository;
    private final ThreatEvidenceRepository threatEvidenceRepository;
    private final InvestigationNoteRepository investigationNoteRepository;

    public ReportService(
            InvestigationRepository investigationRepository,
            LogEntryRepository logEntryRepository,
            ThreatRepository threatRepository,
            ThreatEvidenceRepository threatEvidenceRepository,
            InvestigationNoteRepository investigationNoteRepository) {
        this.investigationRepository = investigationRepository;
        this.logEntryRepository = logEntryRepository;
        this.threatRepository = threatRepository;
        this.threatEvidenceRepository = threatEvidenceRepository;
        this.investigationNoteRepository = investigationNoteRepository;
    }

    public byte[] generatePdfReport(Long investigationId) {
        Investigation inv = investigationRepository.findById(investigationId)
                .orElseThrow(() -> new ResourceNotFoundException("Investigation", "id", investigationId));

        List<LogEntry> logs = logEntryRepository.findByInvestigationIdOrderByTimestampAscIdAsc(investigationId);
        List<Threat> threats = threatRepository.findByInvestigationIdOrderByDetectedAtDesc(investigationId);
        List<InvestigationNote> notes = investigationNoteRepository.findByInvestigationIdOrderByCreatedAtDesc(investigationId);

        log.info("Generating PDF report for investigation {} ('{}')", inv.getId(), inv.getName());

        try (PDDocument document = new PDDocument()) {
            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontOblique = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            // ================= PAGE 1: COVER & EXECUTIVE SUMMARY =================
            PDPage page1 = new PDPage(PDRectangle.A4);
            document.addPage(page1);

            try (PDPageContentStream cs = new PDPageContentStream(document, page1)) {
                float y = 780;

                // Header Banner
                cs.setNonStrokingColor(11 / 255f, 18 / 255f, 32 / 255f); // #0B1220 Navy
                cs.addRect(40, y - 40, 515, 55);
                cs.fill();

                cs.beginText();
                cs.setNonStrokingColor(255 / 255f, 255 / 255f, 255 / 255f);
                cs.setFont(fontBold, 18);
                cs.newLineAtOffset(55, y - 15);
                cs.showText("SENTINELFORENSIC");
                cs.setFont(fontRegular, 10);
                cs.newLineAtOffset(0, -15);
                cs.showText("Digital Incident Triage & Forensic Timeline Analysis Platform");
                cs.endText();

                y -= 70;

                // Title Section
                cs.beginText();
                cs.setNonStrokingColor(15 / 255f, 23 / 255f, 42 / 255f);
                cs.setFont(fontBold, 16);
                cs.newLineAtOffset(45, y);
                cs.showText("FORENSIC INVESTIGATION REPORT");
                cs.endText();

                y -= 25;

                // Investigation Metadata Box
                cs.setNonStrokingColor(241 / 255f, 245 / 255f, 249 / 255f); // Light gray background
                cs.addRect(40, y - 85, 515, 80);
                cs.fill();
                cs.setStrokingColor(203 / 255f, 213 / 255f, 225 / 255f);
                cs.addRect(40, y - 85, 515, 80);
                cs.stroke();

                cs.beginText();
                cs.setNonStrokingColor(15 / 255f, 23 / 255f, 42 / 255f);
                cs.setFont(fontBold, 10);
                cs.newLineAtOffset(55, y - 20);
                cs.showText("Investigation ID: ");
                cs.setFont(fontRegular, 10);
                cs.showText(inv.getInvestigationId());

                cs.newLineAtOffset(240, 0);
                cs.setFont(fontBold, 10);
                cs.showText("Status: ");
                cs.setFont(fontRegular, 10);
                cs.showText(inv.getStatus().name());

                cs.newLineAtOffset(-240, -18);
                cs.setFont(fontBold, 10);
                cs.showText("Investigation Name: ");
                cs.setFont(fontRegular, 10);
                cs.showText(truncate(inv.getName(), 35));

                cs.newLineAtOffset(240, 0);
                cs.setFont(fontBold, 10);
                cs.showText("Generated: ");
                cs.setFont(fontRegular, 10);
                cs.showText(LocalDateTime.now().format(DATE_FMT));

                cs.newLineAtOffset(-240, -18);
                cs.setFont(fontBold, 10);
                cs.showText("Investigator: ");
                cs.setFont(fontRegular, 10);
                cs.showText(inv.getInvestigatorName() != null ? inv.getInvestigatorName() : "Unassigned");

                cs.newLineAtOffset(240, 0);
                cs.setFont(fontBold, 10);
                cs.showText("Classification: ");
                cs.setFont(fontOblique, 10);
                cs.showText("CONFIDENTIAL / LAWFUL FORENSIC");
                cs.endText();

                y -= 110;

                // Executive Summary Section
                cs.beginText();
                cs.setFont(fontBold, 12);
                cs.setNonStrokingColor(37 / 255f, 99 / 255f, 235 / 255f); // Primary blue
                cs.newLineAtOffset(45, y);
                cs.showText("1. EXECUTIVE SUMMARY");
                cs.endText();

                y -= 20;

                long criticalCount = threats.stream().filter(t -> t.getSeverity() == ThreatSeverity.CRITICAL).count();
                long highCount = threats.stream().filter(t -> t.getSeverity() == ThreatSeverity.HIGH).count();
                long mediumCount = threats.stream().filter(t -> t.getSeverity() == ThreatSeverity.MEDIUM).count();
                long lowCount = threats.stream().filter(t -> t.getSeverity() == ThreatSeverity.LOW).count();
                long distinctUsers = logs.stream().map(LogEntry::getUsername).filter(Objects::nonNull).distinct().count();

                cs.beginText();
                cs.setFont(fontRegular, 10);
                cs.setNonStrokingColor(51 / 255f, 65 / 255f, 85 / 255f);
                cs.newLineAtOffset(45, y);
                String summaryText = String.format(
                    "This investigation analyzed %d exported log events and identified %d security threats across %d involved user(s).",
                    logs.size(), threats.size(), distinctUsers
                );
                cs.showText(summaryText);
                cs.newLineAtOffset(0, -15);
                cs.showText(String.format(
                    "Threat Breakdown: Critical: %d  |  High: %d  |  Medium: %d  |  Low: %d.",
                    criticalCount, highCount, mediumCount, lowCount
                ));
                cs.endText();

                y -= 45;

                // Threat Summary Table
                cs.beginText();
                cs.setFont(fontBold, 12);
                cs.setNonStrokingColor(37 / 255f, 99 / 255f, 235 / 255f);
                cs.newLineAtOffset(45, y);
                cs.showText("2. DETECTED THREAT SUMMARY");
                cs.endText();

                y -= 20;

                // Table Header
                cs.setNonStrokingColor(226 / 255f, 232 / 255f, 240 / 255f);
                cs.addRect(40, y - 5, 515, 18);
                cs.fill();

                cs.beginText();
                cs.setFont(fontBold, 9);
                cs.setNonStrokingColor(15 / 255f, 23 / 255f, 42 / 255f);
                cs.newLineAtOffset(45, y);
                cs.showText("RULE CODE");
                cs.newLineAtOffset(110, 0);
                cs.showText("THREAT TITLE");
                cs.newLineAtOffset(150, 0);
                cs.showText("USER");
                cs.newLineAtOffset(75, 0);
                cs.showText("SCORE");
                cs.newLineAtOffset(55, 0);
                cs.showText("SEVERITY");
                cs.endText();

                y -= 18;

                if (threats.isEmpty()) {
                    cs.beginText();
                    cs.setFont(fontOblique, 9);
                    cs.setNonStrokingColor(100 / 255f, 116 / 255f, 139 / 255f);
                    cs.newLineAtOffset(45, y);
                    cs.showText("No security threats were detected based on current rule evaluations.");
                    cs.endText();
                    y -= 20;
                } else {
                    for (Threat t : threats) {
                        if (y < 80) break; // Keep within page bounds

                        cs.beginText();
                        cs.setFont(fontRegular, 8);
                        cs.setNonStrokingColor(15 / 255f, 23 / 255f, 42 / 255f);
                        cs.newLineAtOffset(45, y);
                        cs.showText(t.getRuleCode());
                        cs.newLineAtOffset(110, 0);
                        cs.showText(truncate(t.getTitle(), 28));
                        cs.newLineAtOffset(150, 0);
                        cs.showText(truncate(t.getAffectedUser() != null ? t.getAffectedUser() : "N/A", 12));
                        cs.newLineAtOffset(75, 0);
                        cs.showText(String.valueOf(t.getScore()));
                        cs.newLineAtOffset(55, 0);
                        cs.setFont(fontBold, 8);
                        if (t.getSeverity() == ThreatSeverity.CRITICAL) {
                            cs.setNonStrokingColor(153 / 255f, 27 / 255f, 27 / 255f);
                        } else if (t.getSeverity() == ThreatSeverity.HIGH) {
                            cs.setNonStrokingColor(220 / 255f, 38 / 255f, 38 / 255f);
                        } else {
                            cs.setNonStrokingColor(217 / 255f, 119 / 255f, 6 / 255f);
                        }
                        cs.showText(t.getSeverity().name());
                        cs.endText();

                        y -= 16;
                    }
                }

                // Footer Page 1
                cs.beginText();
                cs.setFont(fontRegular, 8);
                cs.setNonStrokingColor(148 / 255f, 163 / 255f, 184 / 255f);
                cs.newLineAtOffset(45, 30);
                cs.showText("SentinelForensic Confidential Report  |  Page 1 of 2");
                cs.endText();
            }

            // ================= PAGE 2: THREAT DETAILS & TIMELINE EVIDENCE =================
            PDPage page2 = new PDPage(PDRectangle.A4);
            document.addPage(page2);

            try (PDPageContentStream cs2 = new PDPageContentStream(document, page2)) {
                float y = 780;

                cs2.beginText();
                cs2.setFont(fontBold, 12);
                cs2.setNonStrokingColor(37 / 255f, 99 / 255f, 235 / 255f);
                cs2.newLineAtOffset(45, y);
                cs2.showText("3. DETAILED THREAT EVIDENCE & CORRELATION");
                cs2.endText();

                y -= 25;

                if (threats.isEmpty()) {
                    cs2.beginText();
                    cs2.setFont(fontRegular, 9);
                    cs2.setNonStrokingColor(51 / 255f, 65 / 255f, 85 / 255f);
                    cs2.newLineAtOffset(45, y);
                    cs2.showText("No detailed threat evidence to display.");
                    cs2.endText();
                    y -= 30;
                } else {
                    for (Threat t : threats) {
                        if (y < 200) break;

                        // Threat Header Box
                        cs2.setNonStrokingColor(248 / 255f, 250 / 255f, 252 / 255f);
                        cs2.addRect(40, y - 55, 515, 55);
                        cs2.fill();
                        cs2.setStrokingColor(226 / 255f, 232 / 255f, 240 / 255f);
                        cs2.addRect(40, y - 55, 515, 55);
                        cs2.stroke();

                        cs2.beginText();
                        cs2.setFont(fontBold, 10);
                        cs2.setNonStrokingColor(15 / 255f, 23 / 255f, 42 / 255f);
                        cs2.newLineAtOffset(50, y - 15);
                        cs2.showText(t.getTitle() + " (" + t.getRuleCode() + ")");

                        cs2.setFont(fontRegular, 9);
                        cs2.newLineAtOffset(0, -14);
                        cs2.showText("Severity: " + t.getSeverity() + "  |  Detection Score: " + t.getScore() + "  |  Target User: " + (t.getAffectedUser() != null ? t.getAffectedUser() : "N/A"));

                        if (t.getEscalationReason() != null) {
                            cs2.setFont(fontBold, 8);
                            cs2.setNonStrokingColor(153 / 255f, 27 / 255f, 27 / 255f);
                            cs2.newLineAtOffset(0, -12);
                            cs2.showText("Escalation: " + t.getEscalationReason());
                        }
                        cs2.endText();

                        y -= 65;

                        // Explanation
                        cs2.beginText();
                        cs2.setFont(fontRegular, 8);
                        cs2.setNonStrokingColor(51 / 255f, 65 / 255f, 85 / 255f);
                        cs2.newLineAtOffset(50, y);
                        cs2.showText("Explanation: " + truncate(t.getExplanation(), 95));
                        cs2.endText();

                        y -= 16;

                        // Causative evidence events
                        List<Long> evLogIds = threatEvidenceRepository.findLogEntryIdsByThreatId(t.getId());
                        List<LogEntry> evLogs = logEntryRepository.findAllById(evLogIds);

                        cs2.beginText();
                        cs2.setFont(fontBold, 8);
                        cs2.setNonStrokingColor(30 / 255f, 41 / 255f, 59 / 255f);
                        cs2.newLineAtOffset(50, y);
                        cs2.showText("Exact Causative Evidence Events (" + evLogs.size() + " events):");
                        cs2.endText();

                        y -= 14;

                        for (LogEntry ev : evLogs) {
                            if (y < 120) break;
                            cs2.beginText();
                            cs2.setFont(fontRegular, 8);
                            cs2.setNonStrokingColor(71 / 255f, 85 / 255f, 105 / 255f);
                            cs2.newLineAtOffset(60, y);
                            String timeStr = ev.getTimestamp() != null ? ev.getTimestamp().format(DATE_FMT) : "N/A";
                            cs2.showText(String.format("• [%s] %s  User: %s  ->  %s", timeStr, ev.getEventType(), ev.getUsername(), truncate(ev.getRawMessage(), 50)));
                            cs2.endText();
                            y -= 12;
                        }

                        y -= 15;
                    }
                }

                // Section 4: Forensic Conclusion
                if (y >= 140) {
                    cs2.beginText();
                    cs2.setFont(fontBold, 12);
                    cs2.setNonStrokingColor(37 / 255f, 99 / 255f, 235 / 255f);
                    cs2.newLineAtOffset(45, y);
                    cs2.showText("4. FACTUAL FORENSIC CONCLUSION");
                    cs2.endText();

                    y -= 18;

                    cs2.beginText();
                    cs2.setFont(fontRegular, 9);
                    cs2.setNonStrokingColor(51 / 255f, 65 / 255f, 85 / 255f);
                    cs2.newLineAtOffset(45, y);

                    if (logs.isEmpty()) {
                        cs2.showText("No log events were available at the time of report generation.");
                    } else if (threats.isEmpty()) {
                        cs2.showText("The analysis found no deterministic security rule violations in the parsed log sequence.");
                    } else {
                        cs2.showText(String.format(
                            "The forensic analysis identified %d security event(s) requiring investigator follow-up.",
                            threats.size()
                        ));
                        cs2.newLineAtOffset(0, -13);
                        cs2.showText("All evidence links and score contributions have been preserved in accordance with forensic standards.");
                    }
                    cs2.endText();
                }

                // Footer Page 2
                cs2.beginText();
                cs2.setFont(fontRegular, 8);
                cs2.setNonStrokingColor(148 / 255f, 163 / 255f, 184 / 255f);
                cs2.newLineAtOffset(45, 30);
                cs2.showText("SentinelForensic Confidential Report  |  Page 2 of 2");
                cs2.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate PDF report", e);
            throw new RuntimeException("Error rendering PDF report: " + e.getMessage(), e);
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}