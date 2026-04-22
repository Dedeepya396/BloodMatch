package com.example.bloodmatch.hospital;

import com.example.bloodmatch.auth.User;


import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "hospitals")
public class Hospital extends User {
    private String address;
    private double latitude;
    private double longitude;
    private String contactNumber;

    public Hospital() {}

    public Hospital(String name, String address, double latitude, double longitude, String contactNumber) {
        super(name);
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.contactNumber = contactNumber;
    }

    public Hospital(String name, String email, String passwordHash, String address, double latitude, double longitude, String contactNumber) {
        super(name, email, passwordHash);
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.contactNumber = contactNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
