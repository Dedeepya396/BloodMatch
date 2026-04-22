package com.example.bloodmatch.notification;

import java.util.List;

import com.example.bloodmatch.request.BloodRequest;
import com.example.bloodmatch.donor.Donor;

public interface Observer {
    void notify(List<Donor> donors, BloodRequest request);
}
