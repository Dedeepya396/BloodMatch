package com.example.bloodmatch.core;

import com.example.bloodmatch.donor.DonorResponse;
import com.example.bloodmatch.request.BloodRequest;
import com.example.bloodmatch.donor.Donor;
import java.util.List;

public interface MatchingStrategy {
    List<DonorResponse> match(List<Donor> donors, BloodRequest request);
}
