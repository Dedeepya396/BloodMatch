package com.example.bloodmatch.notification;

import com.example.bloodmatch.donor.EligibilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler to run eligibility notifications every Monday at 10 AM.
 */
@Component
public class WeeklyNotificationScheduler {

    @Autowired
    private EligibilityService eligibilityService;

    // Cron expression for Tuesday at 4:36 PM
    @Scheduled(cron = "0 0 10 * * MON")
    public void runWeeklyEligibilityCheck() {
        System.out.println("Starting scheduled weekly eligibility check...");
        eligibilityService.checkEligibilityAndNotify();
    }
}
