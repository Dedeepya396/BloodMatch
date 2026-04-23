package com.example.bloodmatch.controller;

import com.example.bloodmatch.model.BankRequest;
import com.example.bloodmatch.dto.AllocationRequest;
import com.example.bloodmatch.service.BankRequestService;
import com.example.bloodmatch.service.BloodBankService;
import com.example.bloodmatch.service.BloodRequestService;
import com.example.bloodmatch.service.MatchingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bank-requests")
public class BankRequestController {

    private static final Logger logger = LoggerFactory.getLogger(BankRequestController.class);

    private final BankRequestService bankRequestService;
    private final BloodBankService bloodBankService;
    private final BloodRequestService bloodRequestService;
    private final MatchingService matchingService;

    public BankRequestController(BankRequestService bankRequestService, BloodBankService bloodBankService, BloodRequestService bloodRequestService, MatchingService matchingService) {
        this.bankRequestService = bankRequestService;
        this.bloodBankService = bloodBankService;
        this.bloodRequestService = bloodRequestService;
        this.matchingService = matchingService;
    }

    @GetMapping("/bank/{bankId}")
    public List<BankRequest> getPendingForBank(@PathVariable String bankId) {
        return bankRequestService.getPendingForBank(bankId);
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<?> accept(@PathVariable String id) {
        BankRequest br = bankRequestService.findById(id);
        if (br == null) return ResponseEntity.notFound().build();
        if (!"PENDING".equals(br.getStatus())) return ResponseEntity.badRequest().body("Request not pending");

        // attempt allocation
        AllocationRequest allocReq = new AllocationRequest();
        allocReq.setBloodGroup(br.getBloodGroup());
        allocReq.setUnits(br.getUnitsRequested());

        Map<String, Object> allocResult = bloodBankService.allocateBlood(br.getBankId(), allocReq);
        int allocated = (int) allocResult.getOrDefault("allocated", 0);

        br.setAllocatedUnits(allocated);
        br.setStatus("ACCEPTED");
        bankRequestService.save(br);

        // reduce global request units
        int remaining = bloodRequestService.reduceUnits(br.getBloodRequestId(), allocated);

        if (remaining <= 0) {
            // cancel other pending bank requests for this blood request
            bankRequestService.findByBloodRequestId(br.getBloodRequestId()).forEach(other -> {
                if ("PENDING".equals(other.getStatus())) {
                    other.setStatus("CANCELLED");
                    bankRequestService.save(other);
                }
            });
        }

        return ResponseEntity.ok(allocResult);
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable String id) {
        BankRequest br = bankRequestService.findById(id);
        if (br == null) return ResponseEntity.notFound().build();
        if (!"PENDING".equals(br.getStatus())) return ResponseEntity.badRequest().body("Request not pending");
        br.setStatus("REJECTED");
        bankRequestService.save(br);

        // Re-run matching logic for the remaining units
        matchingService.retryMatchingForRequest(br.getBloodRequestId());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/hospital/{hospitalName}/pending")
    public List<BankRequest> getPendingForHospital(@PathVariable String hospitalName) {
        return bankRequestService.findByHospitalName(hospitalName).stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    @GetMapping("/hospital/{hospitalName}/accepted")
    public List<BankRequest> getAcceptedForHospital(@PathVariable String hospitalName) {
        return bankRequestService.findByHospitalName(hospitalName).stream()
                .filter(r -> "ACCEPTED".equals(r.getStatus()))
                .collect(Collectors.toList());
    }
}
