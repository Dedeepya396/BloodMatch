package com.example.bloodmatch.observer;

import com.example.bloodmatch.model.Donor;

/**
 * Interface for notification observers.
 */
public interface NotificationObserver {
    void update(Donor donor, String subject, String messageText);
}
