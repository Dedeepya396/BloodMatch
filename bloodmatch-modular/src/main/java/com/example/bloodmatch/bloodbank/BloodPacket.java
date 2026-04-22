package com.example.bloodmatch.bloodbank;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

/**
 * Represents a single blood packet stored in a blood bank.
 * expiryDate = collectedDate + 42 days (standard shelf life for whole blood).
 * Stored in the "blood_packets" MongoDB collection.
 */
@Document(collection = "blood_packets")
public class BloodPacket {

    @Id
    private String id;

    /** Owner blood bank's MongoDB ID */
    private String bloodBankId;

    /** Blood group: A+, A-, B+, B-, AB+, AB-, O+, O- */
    private String bloodGroup;

    /** Date blood was physically collected from the donor */
    private LocalDate collectedDate;

    /** Date this packet record was added to the system */
    private LocalDate addedDate;

    /** Number of units in this packet */
    private int units;

    /** collectedDate + 42 days — computed on creation */
    private LocalDate expiryDate;

    /**
     * Lifecycle status:
     * AVAILABLE  — in stock and not expired
     * DONATED    — has been used / donated out
     * EXPIRED    — past expiryDate or manually flagged
     */
    private String status;

    public BloodPacket() {}

    public BloodPacket(String bloodBankId, String bloodGroup,
                       LocalDate collectedDate, int units) {
        this.bloodBankId   = bloodBankId;
        this.bloodGroup    = bloodGroup;
        this.collectedDate = collectedDate;
        this.addedDate     = LocalDate.now();
        this.units         = units;
        this.expiryDate    = collectedDate.plusDays(42);
        this.status        = "AVAILABLE";
    }

    // ---------- getters / setters ----------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBloodBankId() { return bloodBankId; }
    public void setBloodBankId(String bloodBankId) { this.bloodBankId = bloodBankId; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public LocalDate getCollectedDate() { return collectedDate; }
    public void setCollectedDate(LocalDate collectedDate) { this.collectedDate = collectedDate; }

    public LocalDate getAddedDate() { return addedDate; }
    public void setAddedDate(LocalDate addedDate) { this.addedDate = addedDate; }

    public int getUnits() { return units; }
    public void setUnits(int units) { this.units = units; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
