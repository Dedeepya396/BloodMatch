package com.example.bloodmatch.request;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "allocation_records")
public class AllocationRecord {

    @Id
    private String id;
    
    private String bloodBankId;
    private String bloodGroup;
    private String originalPacketId;
    private int unitsAllocated;
    private LocalDateTime allocationDate;

    public AllocationRecord() {}

    public AllocationRecord(String bloodBankId, String bloodGroup, String originalPacketId, int unitsAllocated) {
        this.bloodBankId = bloodBankId;
        this.bloodGroup = bloodGroup;
        this.originalPacketId = originalPacketId;
        this.unitsAllocated = unitsAllocated;
        this.allocationDate = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBloodBankId() { return bloodBankId; }
    public void setBloodBankId(String bloodBankId) { this.bloodBankId = bloodBankId; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getOriginalPacketId() { return originalPacketId; }
    public void setOriginalPacketId(String originalPacketId) { this.originalPacketId = originalPacketId; }

    public int getUnitsAllocated() { return unitsAllocated; }
    public void setUnitsAllocated(int unitsAllocated) { this.unitsAllocated = unitsAllocated; }

    public LocalDateTime getAllocationDate() { return allocationDate; }
    public void setAllocationDate(LocalDateTime allocationDate) { this.allocationDate = allocationDate; }
}
