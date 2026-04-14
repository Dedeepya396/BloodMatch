package com.example.bloodmatch.service;

import com.example.bloodmatch.model.BloodRequest;
import com.example.bloodmatch.repository.BloodRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BloodRequestService {

    private final BloodRequestRepository requestRepository;

    public BloodRequestService(BloodRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    // Save request
    public BloodRequest saveRequest(BloodRequest request) {
        return requestRepository.save(request);
    }

    // Get all requests
    public List<BloodRequest> getAllRequests() {
        return requestRepository.findAll();
    }
}