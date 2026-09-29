package com.booking.dto.response;

import com.booking.model.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReservationResponse {

    private Long id;
    private Long userId;
    private String username;
    private Long resourceId;
    private String resourceName;
    private String resourceType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ReservationResponse() {}

    public ReservationResponse(Long id, Long userId, String username, Long resourceId,
                               String resourceName, String resourceType,
                               LocalDateTime startTime, LocalDateTime endTime,
                               ReservationStatus status, BigDecimal price,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.price = price;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Long userId;
        private String username;
        private Long resourceId;
        private String resourceName;
        private String resourceType;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private ReservationStatus status;
        private BigDecimal price;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder resourceId(Long resourceId) { this.resourceId = resourceId; return this; }
        public Builder resourceName(String resourceName) { this.resourceName = resourceName; return this; }
        public Builder resourceType(String resourceType) { this.resourceType = resourceType; return this; }
        public Builder startTime(LocalDateTime startTime) { this.startTime = startTime; return this; }
        public Builder endTime(LocalDateTime endTime) { this.endTime = endTime; return this; }
        public Builder status(ReservationStatus status) { this.status = status; return this; }
        public Builder price(BigDecimal price) { this.price = price; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public ReservationResponse build() {
            return new ReservationResponse(id, userId, username, resourceId, resourceName,
                    resourceType, startTime, endTime, status, price, createdAt, updatedAt);
        }
    }
}
