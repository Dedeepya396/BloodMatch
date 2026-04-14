package com.example.bloodmatch.strategy;

import com.example.bloodmatch.dto.DonorResponse;
import com.example.bloodmatch.model.BloodRequest;
import com.example.bloodmatch.model.Donor;
import com.example.bloodmatch.util.BloodCompatibilityUtil;
import com.example.bloodmatch.util.DistanceUtil;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
// strategy to get ALL matches (not only exact)
public class SmartMatchingStrategy implements MatchingStrategy {

    @Override
    public List<DonorResponse> match(List<Donor> donors, BloodRequest request) {
        return donors.stream()
                .filter(Donor::isAvailable)
                .filter(d -> d.getLastDonationDate() == null ||
                        d.getLastDonationDate().isBefore(LocalDate.now().minusMonths(3)))
                .filter(d -> BloodCompatibilityUtil.isCompatible(
                        d.getBloodGroup(), request.getBloodGroupRequired()))
                // Map early — compute distance once per donor
                .map(d -> new DonorResponse(
                        d,
                        DistanceUtil.calculate(
                                d.getLatitude(), d.getLongitude(),
                                request.getLatitude(), request.getLongitude()),
                        d.getBloodGroup().equals(request.getBloodGroupRequired())
                ))
                .sorted(Comparator.comparingDouble(DonorResponse::getDistanceKm))
                .limit(10)
                .collect(Collectors.toList());
    }
}