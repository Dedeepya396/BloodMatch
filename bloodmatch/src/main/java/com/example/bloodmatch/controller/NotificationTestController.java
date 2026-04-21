package com.example.bloodmatch.controller;

import com.example.bloodmatch.service.EligibilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to manually trigger notifications for testing.
 */
@RestController
@RequestMapping("/api/test/notifications")
public class NotificationTestController {

    @Autowired
    private EligibilityService eligibilityService;

    @PostMapping("/trigger-eligibility")
    public ResponseEntity<String> triggerEligibilityNotifications() {
        System.out.println("Manual trigger for eligibility notifications received.");
        eligibilityService.checkEligibilityAndNotify();
        return ResponseEntity.ok("Eligibility notification process triggered. Check logs for details.");
    }
}
