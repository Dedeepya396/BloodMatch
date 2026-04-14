package com.example.bloodmatch.dto;

import com.example.bloodmatch.model.Donor;

public class DonorResponse {
    private final Donor donor;
    private final double distanceKm;
    private final boolean exactMatch;

    public DonorResponse(Donor donor, double distanceKm, boolean exactMatch) {
        this.donor = donor;
        this.distanceKm = distanceKm;
        this.exactMatch = exactMatch;
    }

    public Donor getDonor()          { return donor; }
    public double getDistanceKm()    { return distanceKm; }
    public boolean isExactMatch()    { return exactMatch; }
}