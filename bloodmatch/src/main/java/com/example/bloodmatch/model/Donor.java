package com.example.bloodmatch.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "donors")
public class Donor extends User {
    private String bloodGroup;

    private double latitude;
    private double longitude;

    private boolean available;

    private LocalDate lastDonationDate;

    public Donor() {
    }

    public Donor(String name, String bloodGroup, double latitude, double longitude,
            boolean available, LocalDate lastDonationDate) {
        super(name);
        this.bloodGroup = bloodGroup;
        this.latitude = latitude;
        this.longitude = longitude;
        this.available = available;
        this.lastDonationDate = lastDonationDate;
    }

    public Donor(String name, String email, String passwordHash, String bloodGroup, double latitude, double longitude,
                 boolean available, LocalDate lastDonationDate) {
        super(name, email, passwordHash);
        this.bloodGroup = bloodGroup;
        this.latitude = latitude;
        this.longitude = longitude;
        this.available = available;
        this.lastDonationDate = lastDonationDate;
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    public String getBloodGroup() {
        return this.bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isAvailable() {
        return this.available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public LocalDate getLastDonationDate() {
        return this.lastDonationDate;
    }

    public void setLastDonationDate(LocalDate lastDonationDate) {
        this.lastDonationDate = lastDonationDate;
    }
}
