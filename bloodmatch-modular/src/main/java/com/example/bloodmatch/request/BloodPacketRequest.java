package com.example.bloodmatch.request;

/**
 * DTO for adding a new blood packet to a blood bank's inventory.
 */
public class BloodPacketRequest {

    /** Blood group: A+, A-, B+, B-, AB+, AB-, O+, O- */
    private String bloodGroup;

    /** Date the blood was collected from the donor (ISO-8601: yyyy-MM-dd) */
    private String collectedDate;

    /** Number of units in this packet */
    private int units;

    public BloodPacketRequest() {}

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getCollectedDate() { return collectedDate; }
    public void setCollectedDate(String collectedDate) { this.collectedDate = collectedDate; }

    public int getUnits() { return units; }
    public void setUnits(int units) { this.units = units; }
}
