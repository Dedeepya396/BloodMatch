package com.example.bloodmatch.request;

import com.example.bloodmatch.donor.EmergencyDonorAlertService;


import com.example.bloodmatch.request.BloodRequest;
import com.example.bloodmatch.request.BloodRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BloodRequestService {

    private static final Logger logger = LoggerFactory.getLogger(BloodRequestService.class);

    private final BloodRequestRepository requestRepository;

    @Autowired
    private EmergencyDonorAlertService emergencyDonorAlertService;

    public BloodRequestService(BloodRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    /**
     * Save a blood request and, if no blood of the requested group is
     * available anywhere in the network, trigger an emergency donor alert.
     */
    public BloodRequest saveRequest(BloodRequest request) {

        logger.info("Creating blood request: userId={}, bloodGroup={}, urgency={}",
                request.getId(), request.getBloodGroupRequired(), request.getUrgency());

        BloodRequest saved = requestRepository.save(request);

        logger.info("Blood request saved successfully with id={}", saved.getId());

        // --- Type-2 Notification: Emergency donor alert ---
        // Check across ALL blood banks whether stock of the requested blood group
        // is zero. If it is, notify nearby eligible donors automatically.
        try {
            emergencyDonorAlertService.triggerEmergencyDonorAlert(saved);
        } catch (Exception ex) {
            logger.error("Emergency alert check failed for requestId={}: {}", saved.getId(), ex.getMessage(), ex);
        }

        return saved;
    }

    public int reduceUnits(String requestId, int unitsAllocated) {
        return requestRepository.findById(requestId).map(r -> {
            int remaining = Math.max(0, r.getUnitsRequired() - unitsAllocated);
            r.setUnitsRequired(remaining);
            requestRepository.save(r);
            logger.info("Reduced units for request {}: allocated={}, remaining={}", requestId, unitsAllocated, remaining);
            return remaining;
        }).orElse(-1);
    }

    // Get all requests
    public List<BloodRequest> getAllRequests() {

        logger.info("Fetching all blood requests");

        List<BloodRequest> requests = requestRepository.findAll();

        logger.info("Total blood requests fetched: {}", requests.size());

        return requests;
    }
    
}