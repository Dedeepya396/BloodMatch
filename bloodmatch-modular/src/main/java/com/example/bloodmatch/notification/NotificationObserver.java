package com.example.bloodmatch.notification;

import com.example.bloodmatch.donor.Donor;

/**
 * Interface for notification observers.
 */
public interface NotificationObserver {
    void update(Donor donor, String subject, String messageText);
}
