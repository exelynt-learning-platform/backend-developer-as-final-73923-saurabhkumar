package com.booking.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ResourceRequest {

    @NotBlank(message = "Resource name is required")
    private String name;

    private String description;

    @NotBlank(message = "Resource type is required")
    private String type;

    private Boolean available = true;

    public ResourceRequest() {}

    public ResourceRequest(String name, String description, String type, Boolean available) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.available = available;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
}
