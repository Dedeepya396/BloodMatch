package com.example.bloodmatch.service;

import com.example.bloodmatch.model.BloodBank;
import com.example.bloodmatch.model.BloodPacket;
import com.example.bloodmatch.model.BloodRequest;
import com.example.bloodmatch.model.Donor;
import com.example.bloodmatch.model.Hospital;
import com.example.bloodmatch.observer.NotificationManager;
import com.example.bloodmatch.repository.DonorRepository;
import com.example.bloodmatch.util.BloodCompatibilityUtil;
import com.example.bloodmatch.util.DistanceUtil;
import com.example.bloodmatch.util.NotificationContentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Emergency Donor Alert Service — Observer Pattern (Type-2 Notification)
 *
 * Triggered when a blood request arrives and no blood bank can satisfy the
 * request (considering urgency level and blood compatibility).
 *
 * HIGH urgency: only blood banks within RADIUS_KM are checked.
 * LOW  urgency: ALL blood banks are checked (no distance limit).
 *
 * If no bank can satisfy, eligible donors within RADIUS_KM are notified.
 *
 * Eligible donor criteria:
 *   1. Blood group is compatible with the requested group.
 *   2. Last donation date is null OR > 90 days ago.
 *   3. Located within RADIUS_KM of the requesting hospital.
 */
@Service
public class EmergencyDonorAlertService {

    private static final Logger logger = LoggerFactory.getLogger(EmergencyDonorAlertService.class);

    /** Radius in km within which donors are considered nearby */
    private static final double RADIUS_KM = 20.0;

    /** Minimum days since last donation to be eligible */
    private static final int MIN_DAYS_SINCE_DONATION = 90;

    /** All blood groups in the system */
    private static final List<String> ALL_BLOOD_GROUPS =
            Arrays.asList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");

    @Autowired
    private BloodBankService bloodBankService;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private NotificationManager notificationManager;

    /**
     * Check whether any blood bank can satisfy the request (exact or compatible
     * blood), respecting urgency-based distance rules.  If no bank can, alert
     * nearby eligible donors.
     *
     * @param request the incoming BloodRequest
     */
    public void triggerEmergencyDonorAlert(BloodRequest request) {
        String requiredGroup = request.getBloodGroupRequired();
        String urgency = request.getUrgency();
        double reqLat = request.getLatitude();
        double reqLon = request.getLongitude();

        logger.info("Emergency alert check — bloodGroup={}, urgency={}", requiredGroup, urgency);

        // 1. Determine which blood groups are compatible (can donate TO the required group)
        List<String> compatibleGroups = ALL_BLOOD_GROUPS.stream()
                .filter(donorGroup -> BloodCompatibilityUtil.isCompatible(donorGroup, requiredGroup))
                .collect(Collectors.toList());

        logger.info("Compatible donor blood groups for {}: {}", requiredGroup, compatibleGroups);

        // 2. Get blood banks (filtered by distance for HIGH urgency only)
        List<BloodBank> banks = bloodBankService.getAllBloodBanks();

        if ("HIGH".equalsIgnoreCase(urgency)) {
            banks = banks.stream()
                    .filter(bb -> DistanceUtil.calculate(
                            bb.getLatitude(), bb.getLongitude(), reqLat, reqLon) <= RADIUS_KM)
                    .collect(Collectors.toList());
            logger.info("HIGH urgency — {} blood bank(s) within {} km.", banks.size(), RADIUS_KM);
        } else {
            logger.info("LOW urgency — checking all {} blood bank(s).", banks.size());
        }

        // 3. Check if ANY of those banks has AVAILABLE packets of a compatible blood group
        boolean anyBankHasStock = false;
        for (BloodBank bank : banks) {
            for (String compatGroup : compatibleGroups) {
                List<BloodPacket> packets = bloodBankService.getPacketsByGroup(bank.getId(), compatGroup);
                boolean hasAvailable = packets.stream()
                        .anyMatch(p -> "AVAILABLE".equals(p.getStatus()) && p.getUnits() > 0);
                if (hasAvailable) {
                    logger.info("Blood bank '{}' has AVAILABLE {} stock — no emergency alert needed.",
                            bank.getName(), compatGroup);
                    anyBankHasStock = true;
                    break;
                }
            }
            if (anyBankHasStock) break;
        }

        if (anyBankHasStock) {
            logger.info("Emergency alert NOT triggered — compatible stock found in blood banks.");
            return;
        }

        logger.warn("ZERO compatible stock ({}) found in {} bank(s)! Triggering emergency donor alert.",
                requiredGroup, banks.size());

        // 4. Find eligible donors: compatible blood group + >90 days since last donation + within RADIUS_KM
        LocalDate cutoffDate = LocalDate.now().minusDays(MIN_DAYS_SINCE_DONATION);

        List<Donor> compatibleDonors = donorRepository.findByBloodGroupIn(compatibleGroups);
        logger.info("Found {} donor(s) with compatible blood groups.", compatibleDonors.size());

        List<Donor> eligibleDonors = compatibleDonors.stream()
                .filter(donor -> {
                    LocalDate lastDonation = donor.getLastDonationDate();
                    boolean eligible = (lastDonation == null) || !lastDonation.isAfter(cutoffDate);
                    if (!eligible) {
                        logger.debug("Donor {} skipped — donated within 90 days (last: {}).",
                                donor.getName(), lastDonation);
                    }
                    return eligible;
                })
                .filter(donor -> {
                    double distKm = DistanceUtil.calculate(
                            reqLat, reqLon, donor.getLatitude(), donor.getLongitude());
                    boolean nearby = distKm <= RADIUS_KM;
                    if (!nearby) {
                        logger.debug("Donor {} skipped — distance {:.2f} km > {} km.",
                                donor.getName(), distKm, RADIUS_KM);
                    } else {
                        logger.debug("Donor {} qualifies — distance {:.2f} km.", donor.getName(), distKm);
                    }
                    return nearby;
                })
                .collect(Collectors.toList());

        logger.info("{} donor(s) qualify for emergency alert (compatible with {}, within {} km, >90 days).",
                eligibleDonors.size(), requiredGroup, RADIUS_KM);

        if (eligibleDonors.isEmpty()) {
            logger.warn("No eligible donors found nearby for blood group {}.", requiredGroup);
            return;
        }

        // 5. Notify each eligible donor via the Observer pipeline
        for (Donor donor : eligibleDonors) {
            String urgencyLabel = "HIGH".equalsIgnoreCase(urgency) ? "🔴 HIGH EMERGENCY" : "🟡 LOW EMERGENCY";
            String subject = "🚨 URGENT: Blood Donation Needed — " + requiredGroup + " — " + urgencyLabel;
            
            // Fetch hospital details for address
            Hospital hospital = hospitalService.findByName(request.getHospitalName());
            String hospitalAddress = (hospital != null) ? hospital.getAddress() : "Address provided on contact";

            String messageText = buildEmergencyMessage(donor, request, urgency, hospitalAddress);

            logger.info("Sending emergency alert to donor {} ({}).", donor.getName(), donor.getEmail());
            notificationManager.notifyObservers(donor, subject, messageText);
        }

        logger.info("Emergency alert dispatch complete for blood group {} ({} urgency).",
                requiredGroup, urgency);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String buildEmergencyMessage(Donor donor, BloodRequest request, String urgency, String address) {
        String urgencyLabel = "HIGH".equalsIgnoreCase(urgency) ? "🔴 HIGH EMERGENCY" : "🟡 LOW EMERGENCY";
        boolean isExact = donor.getBloodGroup().equals(request.getBloodGroupRequired());
        String matchType = isExact ? "exact match" : "compatible match";
        
        return String.format(
            "Hello <strong>%s</strong>,<br><br>" +
            "<div style='background-color: #fff5f5; border-left: 4px solid #c0392b; padding: 15px; margin-bottom: 20px; border-radius: 4px;'>" +
            "  <strong style='color: #c0392b; font-size: 16px;'>%s BLOOD REQUEST — ACTION REQUIRED!</strong><br>" +
            "  A patient requires blood group <strong>%s</strong> and there is currently <strong>ZERO stock</strong> available in any nearby blood banks." +
            "</div>" +
            "<p>We have identified you as a nearby eligible donor with <strong>%s (%s)</strong>.</p>" +
            "<div style='background-color: #fafafa; padding: 15px; border-radius: 8px; border: 1px solid #eee; margin-bottom: 20px;'>" +
            "  <strong style='color: #2c3e50; border-bottom: 1px solid #eee; display: block; padding-bottom: 5px; margin-bottom: 10px;'>Hospital Request Details:</strong>" +
            "  <table style='width: 100%%; border-collapse: collapse; font-size: 14px;'>" +
            "    <tr><td style='padding: 4px 0; color: #777;'>Hospital:</td><td style='padding: 4px 0;'><strong>%s</strong></td></tr>" +
            "    <tr><td style='padding: 4px 0; color: #777;'>Address:</td><td style='padding: 4px 0;'><strong>%s</strong></td></tr>" +
            "    <tr><td style='padding: 4px 0; color: #777;'>Blood Group Needed:</td><td style='padding: 4px 0;'><strong style='color: #c0392b;'>%s</strong></td></tr>" +
            "    <tr><td style='padding: 4px 0; color: #777;'>Units Required:</td><td style='padding: 4px 0;'><strong>%d</strong></td></tr>" +
            "  </table>" +
            "</div>" +
            "<p style='font-size: 15px;'><strong>You are eligible to donate today!</strong> Please try to donate directly at the hospital at your earliest convenience. Your contribution could save a life right now.</p>" +
            "%s" +
            "<br><br>Thank you,<br><strong>BloodMatch Emergency Team</strong>",
            donor.getName(),
            urgencyLabel,
            request.getBloodGroupRequired(),
            donor.getBloodGroup(),
            matchType,
            request.getHospitalName(),
            address,
            request.getBloodGroupRequired(),
            request.getUnitsRequired(),
            NotificationContentUtil.getDonorGuidelinesHtml()
        );
    }
}
