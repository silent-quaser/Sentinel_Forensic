package com.sentinelforensic.dto;

import java.util.ArrayList;
import java.util.List;

public class DetectionResultDto {
    private int rulesEvaluated;
    private int threatsCreated;
    private int existingThreatsUnchanged;
    private List<ThreatDto> newlyDetectedThreats = new ArrayList<>();
    private String message;

    public DetectionResultDto() {}

    public DetectionResultDto(int rulesEvaluated, int threatsCreated, int existingThreatsUnchanged, String message) {
        this.rulesEvaluated = rulesEvaluated;
        this.threatsCreated = threatsCreated;
        this.existingThreatsUnchanged = existingThreatsUnchanged;
        this.message = message;
    }

    public int getRulesEvaluated() { return rulesEvaluated; }
    public void setRulesEvaluated(int rulesEvaluated) { this.rulesEvaluated = rulesEvaluated; }

    public int getThreatsCreated() { return threatsCreated; }
    public void setThreatsCreated(int threatsCreated) { this.threatsCreated = threatsCreated; }

    public int getExistingThreatsUnchanged() { return existingThreatsUnchanged; }
    public void setExistingThreatsUnchanged(int existingThreatsUnchanged) { this.existingThreatsUnchanged = existingThreatsUnchanged; }

    public List<ThreatDto> getNewlyDetectedThreats() { return newlyDetectedThreats; }
    public void setNewlyDetectedThreats(List<ThreatDto> newlyDetectedThreats) { this.newlyDetectedThreats = newlyDetectedThreats; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
