package com.example.bloodmatch.dto;

import java.time.LocalDate;

public class DonationRequest {
    private String bloodBankId;
    private int units;
    private LocalDate donationDate;

    public DonationRequest() {}

    public String getBloodBankId() { return bloodBankId; }
    public void setBloodBankId(String bloodBankId) { this.bloodBankId = bloodBankId; }

    public int getUnits() { return units; }
    public void setUnits(int units) { this.units = units; }

    public LocalDate getDonationDate() { return donationDate; }
    public void setDonationDate(LocalDate donationDate) { this.donationDate = donationDate; }
}
