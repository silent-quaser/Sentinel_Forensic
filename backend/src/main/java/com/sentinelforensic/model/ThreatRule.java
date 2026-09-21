package com.sentinelforensic.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "threat_rules")
public class ThreatRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String ruleCode;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
    
    private boolean enabled;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ThreatSeverity severity;
    
    @Column(nullable = false)
    private Integer threshold;
    
    @Column(nullable = false)
    private Integer timeWindowSeconds;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.threshold == null) {
            this.threshold = 1;
        }
        if (this.timeWindowSeconds == null) {
            this.timeWindowSeconds = 600;
        }
    }

    public ThreatRule() {}

    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    public String getRuleCode() { return this.ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    public boolean getEnabled() { return this.enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public ThreatSeverity getSeverity() { return this.severity; }
    public void setSeverity(ThreatSeverity severity) { this.severity = severity; }

    public Integer getThreshold() { return this.threshold; }
    public void setThreshold(Integer threshold) { this.threshold = threshold; }

    public Integer getTimeWindowSeconds() { return this.timeWindowSeconds; }
    public void setTimeWindowSeconds(Integer timeWindowSeconds) { this.timeWindowSeconds = timeWindowSeconds; }

    public Integer getTimeWindowMinutes() {
        return this.timeWindowSeconds != null ? this.timeWindowSeconds / 60 : 10;
    }

    public void setTimeWindowMinutes(Integer minutes) {
        this.timeWindowSeconds = (minutes != null ? minutes * 60 : 600);
    }

    public LocalDateTime getCreatedAt() { return this.createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}