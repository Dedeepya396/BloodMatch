package com.example.bloodmatch.observer;

import java.util.List;

import com.example.bloodmatch.model.BloodRequest;
import com.example.bloodmatch.model.Donor;

public interface Observer {
    void notify(List<Donor> donors, BloodRequest request);
}
