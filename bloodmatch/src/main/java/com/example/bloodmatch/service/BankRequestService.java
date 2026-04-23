package com.example.bloodmatch.service;

import com.example.bloodmatch.model.BankRequest;
import com.example.bloodmatch.model.BloodRequest;
import com.example.bloodmatch.model.BloodBank;
import com.example.bloodmatch.repository.BankRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BankRequestService {

    private static final Logger logger = LoggerFactory.getLogger(BankRequestService.class);

    private final BankRequestRepository bankRequestRepository;
    private final BloodRequestService bloodRequestService;
    private final BloodBankService bloodBankService;
    private final MatchingService matchingService;

    public BankRequestService(BankRequestRepository bankRequestRepository, BloodRequestService bloodRequestService, BloodBankService bloodBankService, @Lazy MatchingService matchingService) {
        this.bankRequestRepository = bankRequestRepository;
        this.bloodRequestService = bloodRequestService;
        this.bloodBankService = bloodBankService;
        this.matchingService = matchingService;
    }

    public void createRequestsForBanks(BloodRequest request, List<BloodBank> banks) {
        for (BloodBank b : banks) {
            BankRequest br = new BankRequest(request.getId(), b.getId(), b.getName(), request.getHospitalName(), request.getBloodGroupRequired(), request.getUnitsRequired());
            bankRequestRepository.save(br);
            logger.info("Created bank request {} for bank {} (requestId={})", br.getId(), b.getId(), request.getId());
        }
    }

    /**
     * Create BankRequest entries where each bank is assigned a specific
     * number of units to provide (allocations map: bankId -> units)
     */
    public void createRequestsForBanksWithAllocations(BloodRequest request, java.util.Map<String,Integer> allocations) {
        for (java.util.Map.Entry<String,Integer> e : allocations.entrySet()) {
            String bankId = e.getKey();
            BloodBank bank = bloodBankService.getById(bankId).orElse(null);
            String bankName = (bank != null) ? bank.getName() : "Unknown Bank";
            
            int units = Math.max(0, e.getValue() == null ? 0 : e.getValue());
            BankRequest br = new BankRequest(request.getId(), bankId, bankName, request.getHospitalName(), request.getBloodGroupRequired(), units);
            bankRequestRepository.save(br);
            logger.info("Created allocated bank request {} for bank {} (requestId={}, units={})", br.getId(), bankId, request.getId(), units);
        }
    }

    public List<BankRequest> getPendingForBank(String bankId) {
        List<BankRequest> pending = bankRequestRepository.findByBankIdAndStatus(bankId, "PENDING");
        // Filter out requests that this bank can no longer satisfy due to inventory changes
        // If the bank cannot satisfy a pending request anymore, mark it as CANCELLED
        List<BankRequest> result = new java.util.ArrayList<>();
        for (BankRequest br : pending) {
            int available = bloodBankService.getAvailableUnits(bankId, br.getBloodGroup());
            if (available >= br.getUnitsRequested()) {
                result.add(br);
            } else {
                br.setStatus("CANCELLED");
                bankRequestRepository.save(br);
                logger.info("Auto-cancelled bank request {} for bank {} due to insufficient inventory", br.getId(), bankId);
                
                // Re-run matching logic
                matchingService.retryMatchingForRequest(br.getBloodRequestId());
            }
        }
        return result;
    }

    public BankRequest findById(String id) {
        return bankRequestRepository.findById(id).orElse(null);
    }

    public BankRequest save(BankRequest br) {
        return bankRequestRepository.save(br);
    }

    public List<BankRequest> findByBloodRequestId(String bloodRequestId) {
        return bankRequestRepository.findByBloodRequestId(bloodRequestId);
    }

    public List<BankRequest> findByHospitalName(String hospitalName) {
        return bankRequestRepository.findByHospitalName(hospitalName);
    }
}
