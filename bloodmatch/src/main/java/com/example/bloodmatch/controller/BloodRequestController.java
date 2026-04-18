package com.example.bloodmatch.controller;

import com.example.bloodmatch.dto.DonorResponse;
import com.example.bloodmatch.model.BloodRequest;
// import com.example.bloodmatch.model.Donor;
import com.example.bloodmatch.service.BloodRequestService;
import org.springframework.web.bind.annotation.*;
import com.example.bloodmatch.service.MatchingService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/requests")

public class BloodRequestController {

    private final BloodRequestService requestService;
    private final MatchingService matchingService;
    private static final Logger logger = LoggerFactory.getLogger(BloodRequestService.class);

    public BloodRequestController(BloodRequestService requestService, MatchingService matchingService) {
        this.requestService = requestService;
        this.matchingService = matchingService;
    }

    // 1. Create blood request
    @PostMapping("/match")
    public List<DonorResponse> createRequestAndMatch(@RequestBody BloodRequest request) {
        logger.info("Received a blood request with id:{}, hospital name: {}, required blood group: {}", request.getId(),
                request.getHospitalName(), request.getBloodGroupRequired());
        requestService.saveRequest(request);
        return matchingService.findMatches(request);
    }

    // 2. Get all requests
    @GetMapping
    public List<BloodRequest> getAllRequests() {
        logger.info("Received a request to get all blood requests.");

        return requestService.getAllRequests();
    }
}