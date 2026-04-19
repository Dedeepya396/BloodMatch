package com.example.bloodmatch.model.factory;

import com.example.bloodmatch.model.BloodBank;
import com.example.bloodmatch.model.Donor;
import com.example.bloodmatch.model.Hospital;
import com.example.bloodmatch.model.User;

import java.time.LocalDate;

/**
 * Simple factory for creating user role instances.
 */
public class UserFactory {

    public static Donor createDonor(String name, String email, String passwordHash, String bloodGroup, double latitude,
                                    double longitude, boolean available, LocalDate lastDonationDate) {
        return new Donor(name, email, passwordHash, bloodGroup, latitude, longitude, available, lastDonationDate);
    }

    public static Hospital createHospital(String name, String email, String passwordHash, String address, double latitude,
                                          double longitude, String contactNumber) {
        return new Hospital(name, email, passwordHash, address, latitude, longitude, contactNumber);
    }

    public static BloodBank createBloodBank(String name, String email, String passwordHash,
                                            String address, String contactNumber, double latitude, double longitude) {
        return new BloodBank(name, email, passwordHash, address, contactNumber, latitude, longitude);
    }

    public static User createUserFromRole(String role, Object... params) {
        if ("DONOR".equalsIgnoreCase(role)) {
            return createDonor((String) params[0], (String) params[1], (String) params[2], (String) params[3], (Double) params[4], (Double) params[5], (Boolean) params[6], (LocalDate) params[7]);
        } else if ("HOSPITAL".equalsIgnoreCase(role)) {
            return createHospital((String) params[0], (String) params[1], (String) params[2], (String) params[3], (Double) params[4], (Double) params[5], (String) params[6]);
        } else if ("BLOOD_BANK".equalsIgnoreCase(role)) {
            return createBloodBank((String) params[0], (String) params[1], (String) params[2], (String) params[3], (String) params[4], (Double) params[5], (Double) params[6]);
        }
        throw new IllegalArgumentException("Unknown role: " + role);
    }
}
