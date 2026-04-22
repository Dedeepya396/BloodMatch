package com.example.bloodmatch.request;

public class AllocationRequest {
    private String bloodGroup;
    private int units;

    public AllocationRequest() {}

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public int getUnits() { return units; }
    public void setUnits(int units) { this.units = units; }
}
