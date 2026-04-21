package com.example.bloodmatch.dto;

import java.time.LocalDateTime;

public class HospitalRequestStatus {
    private String requestId;
    private String bloodGroup;
    private int totalRequested;
    private int allocated;
    private int remaining;
    private LocalDateTime createdAt;

    public HospitalRequestStatus() {}

    public HospitalRequestStatus(String requestId, String bloodGroup, int totalRequested, int allocated, int remaining, LocalDateTime createdAt) {
        this.requestId = requestId;
        this.bloodGroup = bloodGroup;
        this.totalRequested = totalRequested;
        this.allocated = allocated;
        this.remaining = remaining;
        this.createdAt = createdAt;
    }

    public String getRequestId() { return requestId; }
    public String getBloodGroup() { return bloodGroup; }
    public int getTotalRequested() { return totalRequested; }
    public int getAllocated() { return allocated; }
    public int getRemaining() { return remaining; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setRequestId(String requestId) { this.requestId = requestId; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public void setTotalRequested(int totalRequested) { this.totalRequested = totalRequested; }
    public void setAllocated(int allocated) { this.allocated = allocated; }
    public void setRemaining(int remaining) { this.remaining = remaining; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
