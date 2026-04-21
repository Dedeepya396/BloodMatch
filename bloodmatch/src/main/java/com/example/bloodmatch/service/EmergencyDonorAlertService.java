package com.example.bloodmatch.service;

import com.example.bloodmatch.model.BloodRequest;
import com.example.bloodmatch.model.Donor;
import com.example.bloodmatch.observer.NotificationManager;
import com.example.bloodmatch.repository.BloodPacketRepository;
import com.example.bloodmatch.repository.DonorRepository;
import com.example.bloodmatch.util.DistanceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Emergency Donor Alert Service — Observer Pattern (Type-2 Notification)
 *
 * Triggered when a blood request arrives and ZERO AVAILABLE packets of the
 * requested blood group exist across ALL blood banks in the network.
 *
 * Criteria for an eligible donor:
 *   1. Blood group matches the requested blood group.
 *   2. Last donation date is null OR > 90 days ago.
 *   3. Located within 20 km of the requesting hospital.
 *
 * The email is sent from the shared admin address (jagadeeshamudala.111@gmail.com)
 * via the existing EmailNotificationObserver registered in NotificationManager.
 */
@Service
public class EmergencyDonorAlertService {

    private static final Logger logger = LoggerFactory.getLogger(EmergencyDonorAlertService.class);

    /** Radius in km within which donors are considered nearby */
    private static final double RADIUS_KM = 20.0;

    /** Minimum days since last donation to be eligible */
    private static final int MIN_DAYS_SINCE_DONATION = 90;

    @Autowired
    private BloodPacketRepository bloodPacketRepository;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private NotificationManager notificationManager;

    /**
     * Check if a zero-stock condition exists for the requested blood group
     * across all blood banks, and if so, alert nearby eligible donors.
     *
     * @param request the incoming BloodRequest (must contain bloodGroupRequired,
     *                latitude, longitude, and hospitalName)
     */
    public void triggerIfNetworkStockEmpty(BloodRequest request) {
        String bloodGroup = request.getBloodGroupRequired();

        long availableCount = bloodPacketRepository.countByBloodGroupAndStatus(bloodGroup, "AVAILABLE");

        if (availableCount > 0) {
            logger.info("Emergency alert NOT triggered: {} AVAILABLE packets of {} found across all banks.",
                    availableCount, bloodGroup);
            return;
        }

        logger.warn("ZERO stock of {} across ALL blood banks! Triggering emergency donor alert.", bloodGroup);

        // Eligibility cutoff: 90 days ago
        LocalDate cutoffDate = LocalDate.now().minusDays(MIN_DAYS_SINCE_DONATION);

        // Fetch all donors with matching blood group
        List<Donor> matchingDonors = donorRepository.findByBloodGroup(bloodGroup);
        logger.info("Found {} donor(s) with blood group {}.", matchingDonors.size(), bloodGroup);

        // Apply eligibility + proximity filters
        List<Donor> eligibleDonors = matchingDonors.stream()
                .filter(donor -> {
                    // 90-day eligibility check
                    LocalDate lastDonation = donor.getLastDonationDate();
                    boolean eligible = (lastDonation == null) || !lastDonation.isAfter(cutoffDate);
                    if (!eligible) {
                        logger.debug("Donor {} skipped — donated within 90 days (last: {}).",
                                donor.getName(), lastDonation);
                    }
                    return eligible;
                })
                .filter(donor -> {
                    // 20 km radius check
                    double distKm = DistanceUtil.calculate(
                            request.getLatitude(), request.getLongitude(),
                            donor.getLatitude(), donor.getLongitude());
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

        logger.info("{} donor(s) qualify for emergency alert (blood group={}, within {} km, >90 days).",
                eligibleDonors.size(), bloodGroup, RADIUS_KM);

        if (eligibleDonors.isEmpty()) {
            logger.warn("No eligible donors found nearby for blood group {}.", bloodGroup);
            return;
        }

        // Notify each eligible donor via the existing Observer pipeline
        for (Donor donor : eligibleDonors) {
            String subject = "🚨 URGENT: Blood Donation Needed — " + bloodGroup + " — BloodMatch";
            String messageText = buildEmergencyMessage(donor, request);

            logger.info("Sending emergency alert to donor {} ({}).", donor.getName(), donor.getEmail());
            notificationManager.notifyObservers(donor, subject, messageText);
        }

        logger.info("Emergency alert dispatch complete for blood group {}.", bloodGroup);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String buildEmergencyMessage(Donor donor, BloodRequest request) {
        return String.format(
            "Hello %s,\n\n" +
            "🚨 URGENT BLOOD REQUEST 🚨\n\n" +
            "Hospital: %s\n" +
            "Blood Group Required: %s\n" +
            "Units Required: %d\n\n" +
            "There is currently NO stock of %s blood available in any blood bank " +
            "in our network. You are one of the nearest eligible donors who can help.\n\n" +
            "You last donated more than 90 days ago (or are a first-time donor), which means " +
            "you are eligible to donate today!\n\n" +
            "Please contact the hospital or your nearest blood bank as soon as possible.\n\n" +
            "Your donation could save a life right now.\n\n" +
            "Thank you,\n" +
            "BloodMatch Emergency Team",
            donor.getName(),
            request.getHospitalName(),
            request.getBloodGroupRequired(),
            request.getUnitsRequired(),
            request.getBloodGroupRequired()
        );
    }
}
