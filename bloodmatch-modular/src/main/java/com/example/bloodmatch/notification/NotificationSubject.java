package com.example.bloodmatch.notification;

import com.example.bloodmatch.donor.Donor;

/**
 * Interface for notification subject.
 */
public interface NotificationSubject {
    void registerObserver(NotificationObserver observer);
    void removeObserver(NotificationObserver observer);
    void notifyObservers(Donor donor, String subject, String messageText);
}
