package com.example.bloodmatch.observer;

import com.example.bloodmatch.model.Donor;

/**
 * Interface for notification subject.
 */
public interface NotificationSubject {
    void registerObserver(NotificationObserver observer);
    void removeObserver(NotificationObserver observer);
    void notifyObservers(Donor donor, String subject, String messageText);
}
