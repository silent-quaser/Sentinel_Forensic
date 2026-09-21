package com.sentinelforensic.dto;

import com.sentinelforensic.model.ThreatSeverity;
import com.sentinelforensic.model.ThreatStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ThreatDto {
    private Long id;
    private Long investigationId;
    private String ruleCode;
    private String title;
    private String description;
    private String explanation;
    private ThreatSeverity severity;
    private Integer score;
    private ThreatStatus status;
    private LocalDateTime firstObserved;
    private LocalDateTime lastObserved;
    private LocalDateTime detectedAt;
    private String affectedUser;
    private String affectedIp;
    private String escalationReason;
    private String scoreBreakdown;
    private List<LogEntryDto> evidenceEvents = new ArrayList<>();

    public ThreatDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInvestigationId() { return investigationId; }
    public void setInvestigationId(Long investigationId) { this.investigationId = investigationId; }

    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public ThreatSeverity getSeverity() { return severity; }
    public void setSeverity(ThreatSeverity severity) { this.severity = severity; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public ThreatStatus getStatus() { return status; }
    public void setStatus(ThreatStatus status) { this.status = status; }

    public LocalDateTime getFirstObserved() { return firstObserved; }
    public void setFirstObserved(LocalDateTime firstObserved) { this.firstObserved = firstObserved; }

    public LocalDateTime getLastObserved() { return lastObserved; }
    public void setLastObserved(LocalDateTime lastObserved) { this.lastObserved = lastObserved; }

    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }

    public String getAffectedUser() { return affectedUser; }
    public void setAffectedUser(String affectedUser) { this.affectedUser = affectedUser; }

    public String getAffectedIp() { return affectedIp; }
    public void setAffectedIp(String affectedIp) { this.affectedIp = affectedIp; }

    public String getEscalationReason() { return escalationReason; }
    public void setEscalationReason(String escalationReason) { this.escalationReason = escalationReason; }

    public String getScoreBreakdown() { return scoreBreakdown; }
    public void setScoreBreakdown(String scoreBreakdown) { this.scoreBreakdown = scoreBreakdown; }

    public List<LogEntryDto> getEvidenceEvents() { return evidenceEvents; }
    public void setEvidenceEvents(List<LogEntryDto> evidenceEvents) { this.evidenceEvents = evidenceEvents; }
}
