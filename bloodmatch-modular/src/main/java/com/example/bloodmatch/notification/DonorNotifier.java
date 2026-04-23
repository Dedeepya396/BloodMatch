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
import com.example.bloodmatch.core.NotificationContentUtil;

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
                "Hello <strong>%s</strong>,<br><br>" +
                "<div style='background-color: #fff5f5; border-left: 4px solid #c0392b; padding: 15px; margin-bottom: 20px; border-radius: 4px;'>" +
                "  <strong style='color: #c0392b; font-size: 16px;'>%s BLOOD REQUEST — ACTION REQUIRED!</strong><br>" +
                "  A patient requires blood group <strong>%s</strong> and there is currently <strong>ZERO stock</strong> available in any nearby blood banks." +
                "</div>" +
                "<p>We have identified you as a nearby eligible donor with <strong>%s (%s)</strong>.</p>" +
                "<div style='background-color: #fafafa; padding: 15px; border-radius: 8px; border: 1px solid #eee; margin-bottom: 20px;'>" +
                "  <strong style='color: #2c3e50; border-bottom: 1px solid #eee; display: block; padding-bottom: 5px; margin-bottom: 10px;'>Hospital Request Details:</strong>" +
                "  <table style='width: 100%%; border-collapse: collapse; font-size: 14px;'>" +
                "    <tr><td style='padding: 4px 0; color: #777;'>Hospital:</td><td style='padding: 4px 0;'><strong>%s</strong></td></tr>" +
                "    <tr><td style='padding: 4px 0; color: #777;'>Address:</td><td style='padding: 4px 0;'><strong>%s</strong></td></tr>" +
                "    <tr><td style='padding: 4px 0; color: #777;'>Blood Group Needed:</td><td style='padding: 4px 0;'><strong style='color: #c0392b;'>%s</strong></td></tr>" +
                "    <tr><td style='padding: 4px 0; color: #777;'>Units Required:</td><td style='padding: 4px 0;'><strong>%d</strong></td></tr>" +
                "  </table>" +
                "</div>" +
                "<p style='font-size: 15px;'><strong>You are eligible to donate today!</strong> Please try to donate directly at the hospital at your earliest convenience. Your contribution could save a life right now.</p>" +
                "%s" +
                "<br><br>Thank you,<br><strong>BloodMatch Emergency Team</strong>",
                donor.getName(),
                urgencyLabel,
                requiredGroup,
                donor.getBloodGroup(),
                matchType,
                request.getHospitalName(),
                hospitalAddress,
                requiredGroup,
                request.getUnitsRequired(),
                NotificationContentUtil.getDonorGuidelinesHtml()
            );

            notificationManager.notifyObservers(donor, subject, messageText);
        }
    }
}
