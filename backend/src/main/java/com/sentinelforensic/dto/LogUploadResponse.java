package com.sentinelforensic.dto;

import java.util.ArrayList;
import java.util.List;

public class LogUploadResponse {
    private String fileName;
    private int totalLines;
    private int parsedEvents;
    private int skippedLines;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    public LogUploadResponse() {}

    public LogUploadResponse(String fileName, int totalLines, int parsedEvents, int skippedLines) {
        this.fileName = fileName;
        this.totalLines = totalLines;
        this.parsedEvents = parsedEvents;
        this.skippedLines = skippedLines;
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public int getTotalLines() { return totalLines; }
    public void setTotalLines(int totalLines) { this.totalLines = totalLines; }

    public int getParsedEvents() { return parsedEvents; }
    public void setParsedEvents(int parsedEvents) { this.parsedEvents = parsedEvents; }

    public int getSkippedLines() { return skippedLines; }
    public void setSkippedLines(int skippedLines) { this.skippedLines = skippedLines; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    public void addError(String error) {
        this.errors.add(error);
    }
}
