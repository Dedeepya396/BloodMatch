package com.example.bloodmatch.service;

import com.example.bloodmatch.model.BloodRequest;
import com.example.bloodmatch.repository.BloodRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BloodRequestService {

    private static final Logger logger = LoggerFactory.getLogger(BloodRequestService.class);

    private final BloodRequestRepository requestRepository;

    public BloodRequestService(BloodRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    // Save request
    public BloodRequest saveRequest(BloodRequest request) {

        logger.info("Creating blood request: userId={}, bloodGroup={}, urgency={}",
                request.getId(), request.getBloodGroupRequired(), request.getUrgency());

        BloodRequest saved = requestRepository.save(request);

        logger.info("Blood request saved successfully with id={}", saved.getId());

        return saved;
    }

    // Get all requests
    public List<BloodRequest> getAllRequests() {

        logger.info("Fetching all blood requests");

        List<BloodRequest> requests = requestRepository.findAll();

        logger.info("Total blood requests fetched: {}", requests.size());

        return requests;
    }
}