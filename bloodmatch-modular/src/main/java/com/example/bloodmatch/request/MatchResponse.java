package com.example.bloodmatch.request;

import com.example.bloodmatch.donor.Donor;

public class MatchResponse {
    private String type; // "BANK" or "DONOR"

    // Bank-specific fields
    private String bankId;
    private String name;
    private String bloodGroup;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
    private Integer availableUnits;
    private String contactNumber;

    // Donor-specific
    private Donor donor;
    private Boolean exactMatch;

    public MatchResponse() {}

    // Bank constructor
    public MatchResponse(String bankId, String name, String bloodGroup,
                         Double latitude, Double longitude, Double distanceKm,
                         Integer availableUnits, String contactNumber) {
        this.type = "BANK";
        this.bankId = bankId;
        this.name = name;
        this.bloodGroup = bloodGroup;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceKm = distanceKm;
        this.availableUnits = availableUnits;
        this.contactNumber = contactNumber;
    }

    // Donor constructor
    public MatchResponse(Donor donor, Double distanceKm, Boolean exactMatch) {
        this.type = "DONOR";
        this.donor = donor;
        this.name = donor != null ? donor.getName() : null;
        this.bloodGroup = donor != null ? donor.getBloodGroup() : null;
        this.latitude = donor != null ? donor.getLatitude() : null;
        this.longitude = donor != null ? donor.getLongitude() : null;
        this.distanceKm = distanceKm;
        this.exactMatch = exactMatch;
    }

    // Getters / setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getBankId() { return bankId; }
    public void setBankId(String bankId) { this.bankId = bankId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Integer getAvailableUnits() { return availableUnits; }
    public void setAvailableUnits(Integer availableUnits) { this.availableUnits = availableUnits; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public Donor getDonor() { return donor; }
    public void setDonor(Donor donor) { this.donor = donor; }

    public Boolean getExactMatch() { return exactMatch; }
    public void setExactMatch(Boolean exactMatch) { this.exactMatch = exactMatch; }
}
