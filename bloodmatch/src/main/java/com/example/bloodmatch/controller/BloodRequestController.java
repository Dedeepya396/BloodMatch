package com.example.bloodmatch.controller;

import com.example.bloodmatch.dto.DonorResponse;
import com.example.bloodmatch.model.BloodRequest;
// import com.example.bloodmatch.model.Donor;
import com.example.bloodmatch.service.BloodRequestService;
import org.springframework.web.bind.annotation.*;
import com.example.bloodmatch.service.MatchingService;
import java.util.List;

@RestController
@RequestMapping("/api/requests")

public class BloodRequestController {

    private final BloodRequestService requestService;
    private final MatchingService matchingService;

    public BloodRequestController(BloodRequestService requestService, MatchingService matchingService) {
        this.requestService = requestService;
        this.matchingService = matchingService;
    }

    // 1. Create blood request
    @PostMapping("/match")
    public List<DonorResponse> createRequestAndMatch(@RequestBody BloodRequest request) {
        requestService.saveRequest(request);
        return matchingService.findMatches(request);
    }

    // 2. Get all requests
    @GetMapping
    public List<BloodRequest> getAllRequests() {
        return requestService.getAllRequests();
    }
}