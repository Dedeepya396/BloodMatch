package com.example.bloodmatch.bloodbank;

import com.example.bloodmatch.auth.User;


import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a Blood Bank entity in the system.
 * Stored in the "blood_banks" MongoDB collection.
 */
@Document(collection = "blood_banks")
public class BloodBank extends User {

    private String address;
    private String contactNumber;
    private double latitude;
    private double longitude;

    public BloodBank() {}

    public BloodBank(String name, String email, String passwordHash,
                     String address, String contactNumber,
                     double latitude, double longitude) {
        super(name, email, passwordHash);
        this.address = address;
        this.contactNumber = contactNumber;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
