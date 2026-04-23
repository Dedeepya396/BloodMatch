package com.example.bloodmatch.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "blood_requests")
public class BloodRequest {

    @Id
    private String id;

    private String hospitalName;
    private String hospitalId;
    private String status; // PENDING, PARTIALLY_FULFILLED, FULFILLED

    private String bloodGroupRequired;
    private int unitsRequired;

    private double latitude;
    private double longitude;

    private String urgency;

    private LocalDateTime createdAt;

    public BloodRequest() {
        this.status = "PENDING";
    }

    public BloodRequest(String hospitalName, String bloodGroupRequired,
            int unitsRequired, double latitude, double longitude,
            String urgency) {
        this.hospitalName = hospitalName;
        this.bloodGroupRequired = bloodGroupRequired;
        this.unitsRequired = unitsRequired;
        this.latitude = latitude;
        this.longitude = longitude;
        this.urgency = urgency;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }
    
    public String getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBloodGroupRequired() {
        return bloodGroupRequired;
    }

    public void setBloodGroupRequired(String bloodGroupRequired) {
        this.bloodGroupRequired = bloodGroupRequired;
    }

    public int getUnitsRequired() {
        return unitsRequired;
    }

    public void setUnitsRequired(int unitsRequired) {
        this.unitsRequired = unitsRequired;
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

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}