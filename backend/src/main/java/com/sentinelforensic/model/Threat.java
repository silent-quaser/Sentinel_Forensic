package com.sentinelforensic.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "threats")
public class Threat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long investigationId;
    
    @Column(nullable = false)
    private String ruleCode;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String explanation;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ThreatSeverity severity;
    
    @Column(nullable = false)
    private Integer score; // Detection Score, e.g. 65
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ThreatStatus status;
    
    private LocalDateTime firstObserved;
    private LocalDateTime lastObserved;
    
    @Column(nullable = false)
    private LocalDateTime detectedAt;
    
    private String affectedUser;
    private String affectedIp;
    
    @Column(columnDefinition = "TEXT")
    private String escalationReason;
    
    @Column(columnDefinition = "TEXT")
    private String scoreBreakdown;
    
    @PrePersist
    public void prePersist() {
        if (this.detectedAt == null) {
            this.detectedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = ThreatStatus.OPEN;
        }
        if (this.score == null) {
            this.score = 0;
        }
    }

    public Threat() {}

    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    public Long getInvestigationId() { return this.investigationId; }
    public void setInvestigationId(Long investigationId) { this.investigationId = investigationId; }

    public String getRuleCode() { return this.ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }

    public String getTitle() { return this.title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    public String getExplanation() { return this.explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public ThreatSeverity getSeverity() { return this.severity; }
    public void setSeverity(ThreatSeverity severity) { this.severity = severity; }

    public Integer getScore() { return this.score; }
    public void setScore(Integer score) { this.score = score; }

    public ThreatStatus getStatus() { return this.status; }
    public void setStatus(ThreatStatus status) { this.status = status; }

    public LocalDateTime getFirstObserved() { return this.firstObserved; }
    public void setFirstObserved(LocalDateTime firstObserved) { this.firstObserved = firstObserved; }

    public LocalDateTime getLastObserved() { return this.lastObserved; }
    public void setLastObserved(LocalDateTime lastObserved) { this.lastObserved = lastObserved; }

    public LocalDateTime getDetectedAt() { return this.detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }

    public String getAffectedUser() { return this.affectedUser; }
    public void setAffectedUser(String affectedUser) { this.affectedUser = affectedUser; }

    public String getAffectedIp() { return this.affectedIp; }
    public void setAffectedIp(String affectedIp) { this.affectedIp = affectedIp; }

    public String getEscalationReason() { return this.escalationReason; }
    public void setEscalationReason(String escalationReason) { this.escalationReason = escalationReason; }

    public String getScoreBreakdown() { return this.scoreBreakdown; }
    public void setScoreBreakdown(String scoreBreakdown) { this.scoreBreakdown = scoreBreakdown; }
}