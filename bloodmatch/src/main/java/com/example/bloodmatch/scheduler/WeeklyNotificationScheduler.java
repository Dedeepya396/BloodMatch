package com.example.bloodmatch.scheduler;

import com.example.bloodmatch.service.EligibilityService;
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
    @Scheduled(cron = "0 54 16 * * TUE")
    public void runWeeklyEligibilityCheck() {
        System.out.println("Starting scheduled weekly eligibility check...");
        eligibilityService.checkEligibilityAndNotify();
    }
}
