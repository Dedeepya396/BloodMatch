package com.example.bloodmatch.strategy;

import com.example.bloodmatch.dto.DonorResponse;
import com.example.bloodmatch.model.BloodRequest;
import com.example.bloodmatch.model.Donor;
import com.example.bloodmatch.util.DistanceUtil;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// Strategy for LOW urgency: only exact blood group matches
public class ExactMatchStrategy implements MatchingStrategy {

    @Override
    public List<DonorResponse> match(List<Donor> donors, BloodRequest request) {
        return donors.stream()
                .filter(Donor::isAvailable)
                .filter(d -> d.getLastDonationDate() == null ||
                        d.getLastDonationDate().isBefore(LocalDate.now().minusMonths(3)))
                // Only exact blood group matches for LOW urgency
                .filter(d -> d.getBloodGroup().equals(request.getBloodGroupRequired()))
                .map(d -> new DonorResponse(
                        d,
                        DistanceUtil.calculate(
                                d.getLatitude(), d.getLongitude(),
                                request.getLatitude(), request.getLongitude()),
                        true // exact match by construction
                ))
                .sorted(Comparator.comparingDouble(DonorResponse::getDistanceKm))
                .limit(10)
                .collect(Collectors.toList());
    }
}
