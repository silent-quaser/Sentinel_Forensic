package com.sentinelforensic.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_entries")
public class LogEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long investigationId;
    
    private LocalDateTime timestamp;
    private String eventType;
    private String username;
    private String source;
    private String sourceType;
    private String ipAddress;
    private String hostname;
    private String processName;
    private String processId;
    private String severity;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String rawMessage;

    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    public Long getInvestigationId() { return this.investigationId; }
    public void setInvestigationId(Long investigationId) { this.investigationId = investigationId; }

    public LocalDateTime getTimestamp() { return this.timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getEventType() { return this.eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getUsername() { return this.username; }
    public void setUsername(String username) { this.username = username; }

    public String getSource() { return this.source; }
    public void setSource(String source) { this.source = source; }

    public String getSourceType() { return this.sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getIpAddress() { return this.ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getHostname() { return this.hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public String getProcessName() { return this.processName; }
    public void setProcessName(String processName) { this.processName = processName; }

    public String getProcessId() { return this.processId; }
    public void setProcessId(String processId) { this.processId = processId; }

    public String getSeverity() { return this.severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    public String getRawMessage() { return this.rawMessage; }
    public void setRawMessage(String rawMessage) { this.rawMessage = rawMessage; }

}