package com.example.bloodmatch.notification;

import com.example.bloodmatch.donor.Donor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Concrete subject implementation to manage notification observers.
 */
@Component
public class NotificationManager implements NotificationSubject {

    @Autowired
    private List<NotificationObserver> observers;

    @Override
    public void registerObserver(NotificationObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Donor donor, String subject, String messageText) {
        for (NotificationObserver observer : observers) {
            observer.update(donor, subject, messageText);
        }
    }
}
