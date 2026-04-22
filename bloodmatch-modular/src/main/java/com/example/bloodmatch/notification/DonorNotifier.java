package com.example.bloodmatch.notification;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.bloodmatch.request.BloodRequest;
import com.example.bloodmatch.donor.Donor;
@Component
public class DonorNotifier implements Observer {
    @Override
    public void notify(List<Donor> donors, BloodRequest request) {
        for (Donor donor : donors) {
            System.out.println(
                    "Notification sent to " + donor.getName() +
                            " for blood request: " + request.getBloodGroupRequired());
        }
    }

}
