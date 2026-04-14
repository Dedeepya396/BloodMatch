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
// sends exact matches to the user
public class CompatibilityFirstStrategy implements MatchingStrategy {

    @Override
    public List<DonorResponse> match(List<Donor> donors, BloodRequest request) {
        return donors.stream()
                // if donor is available
                .filter(Donor::isAvailable)
                // if donor can donate
                .filter(d -> d.getLastDonationDate() == null ||
                        d.getLastDonationDate().isBefore(LocalDate.now().minusMonths(3)))
                // checks if there are there are donors with same blood group
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
                .sorted(Comparator
                        // exact matches first (false < true, so negate isExactMatch)
                        .comparing((DonorResponse r) -> !r.isExactMatch())
                        .thenComparingDouble(DonorResponse::getDistanceKm))
                // nearest 10 donors
                .limit(10)
                .collect(Collectors.toList());
    }
}