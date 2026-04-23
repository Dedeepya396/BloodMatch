package com.example.bloodmatch.notification;

import java.util.List;

import com.example.bloodmatch.hospital.Hospital;
import com.example.bloodmatch.hospital.HospitalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.bloodmatch.request.BloodRequest;
import com.example.bloodmatch.donor.Donor;

@Component
public class DonorNotifier implements Observer {
    
    private static final Logger logger = LoggerFactory.getLogger(DonorNotifier.class);

    @Autowired
    private NotificationManager notificationManager;

    @Autowired
    private HospitalService hospitalService;

    @Override
    public void notify(List<Donor> donors, BloodRequest request) {
        for (Donor donor : donors) {
            String requiredGroup = request.getBloodGroupRequired();
            String urgency = request.getUrgency();
            
            System.out.println("Notification triggered for " + donor.getName() + " for blood request: " + requiredGroup);
            
            String urgencyLabel = "HIGH".equalsIgnoreCase(urgency) ? "🔴 HIGH EMERGENCY" : "🟡 LOW EMERGENCY";
            String subject = "🚨 URGENT: Blood Donation Needed — " + requiredGroup + " — " + urgencyLabel;
            
            boolean isExact = donor.getBloodGroup().equals(requiredGroup);
            String matchType = isExact ? "exact match" : "compatible match";
            
            Hospital hospital = hospitalService.findByName(request.getHospitalName());
            String hospitalAddress = (hospital != null) ? hospital.getAddress() : "Address provided on contact";

            String messageText = String.format(
                "Hello %s,\n\n" +
                "%s BLOOD REQUEST — ACTION REQUIRED!\n\n" +
                "This is a crucial alert. A patient requires blood group %s and currently there is ZERO stock of this group (or compatible groups) available in any nearby blood banks in our network.\n\n" +
                "We have found you as an eligible donor with a %s (%s) who is currently located near the hospital.\n\n" +
                "Request Details:\n" +
                "• Hospital: %s\n" +
                "• Hospital Address: %s\n" +
                "• Blood Group Required: %s\n" +
                "• Units Required: %d\n\n" +
                "You are eligible to donate today! Please try to donate directly at the hospital at your earliest convenience. Your contribution makes a huge difference and could save a life right now.\n\n" +
                "Thank you,\n" +
                "BloodMatch Emergency Team",
                donor.getName(),
                urgencyLabel,
                requiredGroup,
                donor.getBloodGroup(),
                matchType,
                request.getHospitalName(),
                hospitalAddress,
                requiredGroup,
                request.getUnitsRequired()
            );

            notificationManager.notifyObservers(donor, subject, messageText);
        }
    }
}
