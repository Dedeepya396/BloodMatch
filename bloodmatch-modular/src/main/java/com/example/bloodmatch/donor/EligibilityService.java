package com.example.bloodmatch.donor;

import com.example.bloodmatch.donor.Donor;
import com.example.bloodmatch.notification.NotificationManager;
import com.example.bloodmatch.donor.DonorRepository;
import com.example.bloodmatch.core.NotificationContentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to handle donor eligibility logic.
 */
@Service
public class EligibilityService {
    private static final Logger logger = LoggerFactory.getLogger(EligibilityService.class);

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private NotificationManager notificationManager;

    public void checkEligibilityAndNotify() {
        LocalDate ninetyDaysAgo = LocalDate.now().minusDays(90);
        
        List<Donor> allDonors = donorRepository.findAll();
        
        List<Donor> eligibleDonors = allDonors.stream()
                .filter(donor -> {
                    LocalDate lastDate = donor.getLastDonationDate();
                    return lastDate == null || !lastDate.isAfter(ninetyDaysAgo);
                })
                .collect(Collectors.toList());

        logger.info("Processing eligibility for {} donors.", eligibleDonors.size());

        for (Donor donor : eligibleDonors) {
            String subject = "Ready to save a life again? - BloodMatch";
            
            String messageText = String.format(
                "Hello <strong>%s</strong>,<br><br>" +
                "<div style='background-color: #f0f7ff; border-left: 4px solid #3498db; padding: 15px; margin-bottom: 20px; border-radius: 4px;'>" +
                "  <strong style='color: #2980b9; font-size: 16px;'>Ready to save a life again?</strong><br>" +
                "  It has been more than <strong>90 days</strong> since your last donation. You are now eligible to donate blood again! Your contribution can make a huge difference." +
                "</div>" +
                "<p>Please visit your nearest blood bank or check the app for active requests.</p>" +
                "%s" +
                "<br><br>Thank you,<br><strong>BloodMatch Team</strong>", 
                donor.getName(),
                NotificationContentUtil.getDonorGuidelinesHtml()
            );
            
            notificationManager.notifyObservers(donor, subject, messageText);
        }
    }
}
