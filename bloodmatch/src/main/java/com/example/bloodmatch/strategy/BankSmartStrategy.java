package com.example.bloodmatch.strategy;

import com.example.bloodmatch.dto.MatchResponse;
import com.example.bloodmatch.model.BloodBank;
import com.example.bloodmatch.model.BloodPacket;
import com.example.bloodmatch.model.BloodRequest;
import com.example.bloodmatch.service.BloodBankService;
import com.example.bloodmatch.util.BloodCompatibilityUtil;

import java.util.List;
import java.util.stream.Collectors;

// Smart bank strategy: consider compatible blood groups as well as exact
public class BankSmartStrategy implements BankMatchingStrategy {

    @Override
    public List<MatchResponse> matchBanks(List<BloodBank> allBanks, BloodRequest request, boolean applyDistanceFilter, BloodBankService bloodBankService) {
        double reqLat = request.getLatitude();
        double reqLon = request.getLongitude();
        String reqGroup = request.getBloodGroupRequired();

        return allBanks.stream()
                .filter(bb -> {
                    if (applyDistanceFilter) {
                        double dist = com.example.bloodmatch.util.DistanceUtil.calculate(
                                bb.getLatitude(), bb.getLongitude(), reqLat, reqLon);
                        if (dist > 20.0) return false;
                    }

                    List<BloodPacket> packets = bloodBankService.getAllPackets(bb.getId());
                    return packets.stream()
                            .filter(p -> BloodCompatibilityUtil.isCompatible(p.getBloodGroup(), reqGroup))
                            .anyMatch(p -> "AVAILABLE".equals(p.getStatus()) && p.getUnits() > 0);
                })
                .map(bb -> {
                    double distance = com.example.bloodmatch.util.DistanceUtil.calculate(
                            bb.getLatitude(), bb.getLongitude(), reqLat, reqLon);

                    List<BloodPacket> packets = bloodBankService.getAllPackets(bb.getId());

                    int available = packets.stream()
                            .filter(p -> BloodCompatibilityUtil.isCompatible(p.getBloodGroup(), reqGroup))
                            .filter(p -> "AVAILABLE".equals(p.getStatus()))
                            .mapToInt(BloodPacket::getUnits)
                            .sum();

                    return new MatchResponse(
                            bb.getId(),
                            bb.getName(),
                            reqGroup,
                            bb.getLatitude(),
                            bb.getLongitude(),
                            distance,
                            available,
                            bb.getContactNumber()
                    );
                })
                .collect(Collectors.toList());
    }
}
