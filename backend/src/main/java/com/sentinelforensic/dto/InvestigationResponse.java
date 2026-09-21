package com.sentinelforensic.dto;

import com.sentinelforensic.model.InvestigationStatus;
import java.time.LocalDateTime;

public class InvestigationResponse {
    private Long id;
    private String investigationId;
    private String name;
    private String investigatorName;
    private String description;
    private InvestigationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long eventCount;
    private long threatCount;
    private long criticalThreatCount;
    private long highThreatCount;

    public InvestigationResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInvestigationId() { return investigationId; }
    public void setInvestigationId(String investigationId) { this.investigationId = investigationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getInvestigatorName() { return investigatorName; }
    public void setInvestigatorName(String investigatorName) { this.investigatorName = investigatorName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public InvestigationStatus getStatus() { return status; }
    public void setStatus(InvestigationStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public long getEventCount() { return eventCount; }
    public void setEventCount(long eventCount) { this.eventCount = eventCount; }

    public long getThreatCount() { return threatCount; }
    public void setThreatCount(long threatCount) { this.threatCount = threatCount; }

    public long getCriticalThreatCount() { return criticalThreatCount; }
    public void setCriticalThreatCount(long criticalThreatCount) { this.criticalThreatCount = criticalThreatCount; }

    public long getHighThreatCount() { return highThreatCount; }
    public void setHighThreatCount(long highThreatCount) { this.highThreatCount = highThreatCount; }
}
