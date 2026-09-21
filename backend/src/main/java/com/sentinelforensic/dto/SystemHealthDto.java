package com.sentinelforensic.dto;

import java.time.LocalDateTime;

public class SystemHealthDto {
    private String backendStatus;
    private String databaseStatus;
    private String detectionEngineStatus;
    private String parserStatus;
    private String applicationVersion;
    private LocalDateTime timestamp;

    public SystemHealthDto() {}

    public SystemHealthDto(String backendStatus, String databaseStatus, String detectionEngineStatus, String parserStatus, String applicationVersion) {
        this.backendStatus = backendStatus;
        this.databaseStatus = databaseStatus;
        this.detectionEngineStatus = detectionEngineStatus;
        this.parserStatus = parserStatus;
        this.applicationVersion = applicationVersion;
        this.timestamp = LocalDateTime.now();
    }

    public String getBackendStatus() { return backendStatus; }
    public void setBackendStatus(String backendStatus) { this.backendStatus = backendStatus; }

    public String getDatabaseStatus() { return databaseStatus; }
    public void setDatabaseStatus(String databaseStatus) { this.databaseStatus = databaseStatus; }

    public String getDetectionEngineStatus() { return detectionEngineStatus; }
    public void setDetectionEngineStatus(String detectionEngineStatus) { this.detectionEngineStatus = detectionEngineStatus; }

    public String getParserStatus() { return parserStatus; }
    public void setParserStatus(String parserStatus) { this.parserStatus = parserStatus; }

    public String getApplicationVersion() { return applicationVersion; }
    public void setApplicationVersion(String applicationVersion) { this.applicationVersion = applicationVersion; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
