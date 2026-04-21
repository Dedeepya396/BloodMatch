package com.example.bloodmatch.strategy;

import com.example.bloodmatch.dto.DonorResponse;
import com.example.bloodmatch.model.BloodRequest;
import com.example.bloodmatch.model.Donor;
import java.util.List;

public interface MatchingStrategy {
    List<DonorResponse> match(List<Donor> donors, BloodRequest request);
}
