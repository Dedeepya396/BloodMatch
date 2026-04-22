package com.example.bloodmatch.bloodbank;

import com.example.bloodmatch.bloodbank.BankRequest;
import com.example.bloodmatch.request.AllocationRequest;
import com.example.bloodmatch.bloodbank.BankRequestService;
import com.example.bloodmatch.bloodbank.BloodBankService;
import com.example.bloodmatch.request.BloodRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bank-requests")
public class BankRequestController {

    private static final Logger logger = LoggerFactory.getLogger(BankRequestController.class);

    private final BankRequestService bankRequestService;
    private final BloodBankService bloodBankService;
    private final BloodRequestService bloodRequestService;

    public BankRequestController(BankRequestService bankRequestService, BloodBankService bloodBankService, BloodRequestService bloodRequestService) {
        this.bankRequestService = bankRequestService;
        this.bloodBankService = bloodBankService;
        this.bloodRequestService = bloodRequestService;
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
        return ResponseEntity.ok().build();
    }
}
