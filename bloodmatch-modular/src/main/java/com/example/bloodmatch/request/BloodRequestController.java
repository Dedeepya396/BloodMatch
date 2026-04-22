package com.example.bloodmatch.request;

import com.example.bloodmatch.request.MatchResponse;
import com.example.bloodmatch.request.BloodRequest;
// import com.example.bloodmatch.donor.Donor;
import com.example.bloodmatch.request.BloodRequestService;
import com.example.bloodmatch.bloodbank.BankRequestService;
import com.example.bloodmatch.hospital.HospitalRequestStatus;
import org.springframework.web.bind.annotation.*;
import com.example.bloodmatch.request.MatchingService;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/requests")
public class BloodRequestController {

    private final BloodRequestService requestService;
    private final MatchingService matchingService;
    private final BankRequestService bankRequestService;
    private static final Logger logger = LoggerFactory.getLogger(BloodRequestService.class);

    public BloodRequestController(BloodRequestService requestService, MatchingService matchingService,
            BankRequestService bankRequestService) {
        this.requestService = requestService;
        this.matchingService = matchingService;
        this.bankRequestService = bankRequestService;
    }

    // 1. Create blood request
    @PostMapping("/match")
    public List<MatchResponse> createRequestAndMatch(@RequestBody BloodRequest request) {
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

    // 3. Get requests for a hospital with allocation summary
    @GetMapping("/hospital/{hospitalName}")
    public List<HospitalRequestStatus> getRequestsForHospital(@PathVariable String hospitalName) {
        List<BloodRequest> reqs = requestService.getAllRequests().stream()
                .filter(r -> hospitalName != null && hospitalName.equals(r.getHospitalName()))
                .collect(Collectors.toList());

        List<HospitalRequestStatus> out = new java.util.ArrayList<>();
        for (BloodRequest r : reqs) {
            int remaining = r.getUnitsRequired();
            if (remaining <= 0) {
                continue;
            }
            int allocated = bankRequestService.findByBloodRequestId(r.getId())
                    .stream()
                    .mapToInt(b -> b.getAllocatedUnits())
                    .sum();
            int total = remaining + allocated;
            logger.info("Remaining for: {}", remaining);
            out.add(new HospitalRequestStatus(r.getId(), r.getBloodGroupRequired(), total, allocated, remaining,
                    r.getCreatedAt()));
        }
        logger.info("Returning {} requests", out.size());
        return out;
    }
}