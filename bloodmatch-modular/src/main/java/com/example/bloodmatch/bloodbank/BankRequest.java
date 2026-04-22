package com.example.bloodmatch.bloodbank;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "bank_requests")
public class BankRequest {
    @Id
    private String id;

    private String bloodRequestId;
    private String bankId;
    private String hospitalName;
    private String bloodGroup;
    private int unitsRequested;
    private int allocatedUnits;
    private String status; // PENDING, ACCEPTED, REJECTED, CANCELLED
    private LocalDateTime createdAt;

    public BankRequest() {}

    public BankRequest(String bloodRequestId, String bankId, String hospitalName, String bloodGroup, int unitsRequested) {
        this.bloodRequestId = bloodRequestId;
        this.bankId = bankId;
        this.hospitalName = hospitalName;
        this.bloodGroup = bloodGroup;
        this.unitsRequested = unitsRequested;
        this.allocatedUnits = 0;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBloodRequestId() { return bloodRequestId; }
    public void setBloodRequestId(String bloodRequestId) { this.bloodRequestId = bloodRequestId; }

    public String getBankId() { return bankId; }
    public void setBankId(String bankId) { this.bankId = bankId; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public int getUnitsRequested() { return unitsRequested; }
    public void setUnitsRequested(int unitsRequested) { this.unitsRequested = unitsRequested; }

    public int getAllocatedUnits() { return allocatedUnits; }
    public void setAllocatedUnits(int allocatedUnits) { this.allocatedUnits = allocatedUnits; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
