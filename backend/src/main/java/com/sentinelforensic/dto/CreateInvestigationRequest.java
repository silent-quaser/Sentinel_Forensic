package com.sentinelforensic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateInvestigationRequest {
    @NotBlank(message = "Investigation name is required")
    @Size(min = 3, max = 255, message = "Investigation name must be between 3 and 255 characters")
    private String name;

    @NotBlank(message = "Investigator name is required")
    @Size(min = 2, max = 255, message = "Investigator name must be between 2 and 255 characters")
    private String investigatorName;

    private String description;

    public CreateInvestigationRequest() {}

    public CreateInvestigationRequest(String name, String investigatorName, String description) {
        this.name = name;
        this.investigatorName = investigatorName;
        this.description = description;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getInvestigatorName() { return investigatorName; }
    public void setInvestigatorName(String investigatorName) { this.investigatorName = investigatorName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
