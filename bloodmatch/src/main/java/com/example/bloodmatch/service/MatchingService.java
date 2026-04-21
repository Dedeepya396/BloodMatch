package com.example.bloodmatch.service;

import com.example.bloodmatch.dto.DonorResponse;
import com.example.bloodmatch.dto.MatchResponse;
import com.example.bloodmatch.model.BloodRequest;
import com.example.bloodmatch.model.Donor;
import com.example.bloodmatch.observer.DonorNotifier;
import com.example.bloodmatch.repository.DonorRepository;
import com.example.bloodmatch.service.BloodBankService;
import com.example.bloodmatch.model.BloodBank;
import com.example.bloodmatch.model.BloodPacket;
import com.example.bloodmatch.service.BankRequestService;
import com.example.bloodmatch.strategy.CompatibilityFirstStrategy;
import com.example.bloodmatch.strategy.MatchingStrategy;
import com.example.bloodmatch.strategy.SmartMatchingStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MatchingService {

    private final DonorRepository donorRepository;
    private final DonorNotifier donorNotifier;
        private final BloodBankService bloodBankService;
        private final BankRequestService bankRequestService;
    private static final Logger logger = LoggerFactory.getLogger(MatchingService.class);

        public MatchingService(DonorRepository donorRepository, DonorNotifier donorNotifier,
                        BloodBankService bloodBankService, BankRequestService bankRequestService) {
                this.donorRepository = donorRepository;
                this.donorNotifier = donorNotifier;
                this.bloodBankService = bloodBankService;
                this.bankRequestService = bankRequestService;
        }

    public List<MatchResponse> findMatches(BloodRequest request) {
        logger.info("Starting donor matching for requestId={}, bloodGroup={}, urgency={}",
                request.getId(), request.getBloodGroupRequired(), request.getUrgency());

        MatchingStrategy strategy = "HIGH".equalsIgnoreCase(request.getUrgency())
                ? new SmartMatchingStrategy()
                : new CompatibilityFirstStrategy();
        logger.debug("Selected matching strategy: {}", strategy.getClass().getSimpleName());

        // Only consider blood banks when urgency is HIGH.
        double reqLat = request.getLatitude();
        double reqLon = request.getLongitude();
        String reqGroup = request.getBloodGroupRequired();
        List<BloodBank> allBanks = bloodBankService.getAllBloodBanks();
        List<MatchResponse> bankMatches;
        logger.info("TOTAL BANKS FOUND = {}", allBanks.size());
        if ("HIGH".equalsIgnoreCase(request.getUrgency())) {

            // HIGH urgency → 20km restriction
            bankMatches = allBanks.stream()
                    .filter(bb -> {
                        double dist = com.example.bloodmatch.util.DistanceUtil.calculate(
                                bb.getLatitude(), bb.getLongitude(), reqLat, reqLon);

                        if (dist > 20.0)
                            return false;

                        List<BloodPacket> packets = bloodBankService.getPacketsByGroup(bb.getId(), reqGroup);

                        return packets.stream()
                                .anyMatch(p -> "AVAILABLE".equals(p.getStatus()) && p.getUnits() > 0);
                    })
                    .map(bb -> {
                        double distance = com.example.bloodmatch.util.DistanceUtil.calculate(
                                bb.getLatitude(), bb.getLongitude(), reqLat, reqLon);

                        List<BloodPacket> packets = bloodBankService.getPacketsByGroup(bb.getId(), reqGroup);

                        int available = packets.stream()
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
                                bb.getContactNumber());
                    })
                    .collect(Collectors.toList());

        } else {

            // LOW urgency → NO 20km restriction
            bankMatches = allBanks.stream()
                    .filter(bb -> {

                        List<BloodPacket> packets = bloodBankService.getPacketsByGroup(bb.getId(), reqGroup);

                        return packets.stream()
                                .anyMatch(p -> "AVAILABLE".equals(p.getStatus()) && p.getUnits() > 0);
                    })
                    .map(bb -> {

                        double distance = com.example.bloodmatch.util.DistanceUtil.calculate(
                                bb.getLatitude(), bb.getLongitude(), reqLat, reqLon);

                        List<BloodPacket> packets = bloodBankService.getPacketsByGroup(bb.getId(), reqGroup);

                        int available = packets.stream()
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
                                bb.getContactNumber());
                    })
                                        .collect(Collectors.toList());
            logger.info("Matched banks are {}", bankMatches.size());

        }
                if (!bankMatches.isEmpty()) {
                        int requiredUnits = request.getUnitsRequired();

                        // Banks that can individually satisfy the entire request
                        List<MatchResponse> singleSatisfiers = bankMatches.stream()
                                        .filter(m -> m.getAvailableUnits() != null && m.getAvailableUnits() >= requiredUnits)
                                        .collect(Collectors.toList());

                        if (!singleSatisfiers.isEmpty()) {
                                // create requests for these banks and return only them
                                List<String> ids = singleSatisfiers.stream().map(MatchResponse::getBankId).collect(Collectors.toList());
                                List<BloodBank> matchedBanks = allBanks.stream().filter(b -> ids.contains(b.getId())).collect(Collectors.toList());
                                bankRequestService.createRequestsForBanks(request, matchedBanks);
                                return singleSatisfiers;
                        }

                        // No single bank can satisfy. Check if multiple banks together can fulfill.
                        int totalAvailable = bankMatches.stream().mapToInt(m -> m.getAvailableUnits() == null ? 0 : m.getAvailableUnits()).sum();
                        if (totalAvailable >= requiredUnits) {
                            // choose nearest banks until sum >= requiredUnits
                            List<MatchResponse> selected = bankMatches.stream()
                                    .sorted((a, b) -> Double.compare(a.getDistanceKm() == null ? Double.MAX_VALUE : a.getDistanceKm(), b.getDistanceKm() == null ? Double.MAX_VALUE : b.getDistanceKm()))
                                    .collect(Collectors.toList());

                            List<MatchResponse> toRequest = new java.util.ArrayList<>();
                            int acc = 0;
                            java.util.Map<String,Integer> allocations = new java.util.HashMap<>();
                            for (MatchResponse m : selected) {
                                if (acc >= requiredUnits) break;
                                int avail = m.getAvailableUnits() == null ? 0 : m.getAvailableUnits();
                                if (avail <= 0) continue;
                                int need = Math.max(0, requiredUnits - acc);
                                int assign = Math.min(avail, need);
                                allocations.put(m.getBankId(), assign);
                                acc += assign;
                                toRequest.add(m);
                            }

                            // persist bank requests with per-bank assigned units
                            bankRequestService.createRequestsForBanksWithAllocations(request, allocations);
                            return toRequest;
                        }

                        // If neither single nor combined banks can fulfill, fall through to donors
                }
        // If no banks could satisfy, fall back to donors.
        List<Donor> donors = donorRepository.findAll();
        logger.debug("Fetched {} donors from database", donors.size());

        // For HIGH urgency — only donors within 20km. For LOW urgency — consider all
        // donors.
        if ("HIGH".equalsIgnoreCase(request.getUrgency())) {
            final double MAX_KM = 20.0;
            donors = donors.stream()
                    .filter(d -> com.example.bloodmatch.util.DistanceUtil.calculate(
                            d.getLatitude(), d.getLongitude(), reqLat, reqLon) <= MAX_KM)
                    .collect(Collectors.toList());
            logger.debug("Filtered donors to {} within {} km for HIGH urgency", donors.size(), MAX_KM);
        }

        List<DonorResponse> matched = strategy.match(donors, request);

        logger.info("Matching completed for requestId={}, matchedDonors={}",
                request.getId(), matched.size());

        // Notify donors only for donor matches
        try {
            List<Donor> donorList = matched.stream()
                    .map(DonorResponse::getDonor)
                    .collect(Collectors.toList());

            logger.info("Sending notifications to {} donors for requestId={}",
                    donorList.size(), request.getId());

            donorNotifier.notify(donorList, request);

            logger.info("Notifications sent successfully for requestId={}", request.getId());

        } catch (Exception ex) {
            logger.error("Failed to send notifications for requestId={}", request.getId(), ex);
        }

        // Convert DonorResponse list to MatchResponse list for return
        List<MatchResponse> donorResponses = matched.stream()
                .map(dr -> new MatchResponse(dr.getDonor(), dr.getDistanceKm(), dr.isExactMatch()))
                .collect(Collectors.toList());

        return donorResponses;
    }
}