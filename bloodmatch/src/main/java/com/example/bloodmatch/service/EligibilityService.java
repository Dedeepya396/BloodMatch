package com.example.bloodmatch.service;

import com.example.bloodmatch.model.Donor;
import com.example.bloodmatch.observer.NotificationManager;
import com.example.bloodmatch.repository.DonorRepository;
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
                "It has been more than <strong>90 days</strong> since your last donation (or you are a new potential donor). " +
                "You are now eligible to donate blood again! Your contribution can make a huge difference.<br><br>" +
                "Please visit your nearest blood bank or check the app for active requests.<br><br>" +
                "Thank you,<br><strong>BloodMatch Team</strong>", 
                donor.getName()
            );
            
            notificationManager.notifyObservers(donor, subject, messageText);
        }
    }
}
