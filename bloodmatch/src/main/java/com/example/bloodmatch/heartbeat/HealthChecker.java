package com.example.bloodmatch.heartbeat;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.example.bloodmatch.service.*;
import com.example.bloodmatch.service.BloodBankService;

@Component
public class HealthChecker {

    private final DonorService donorService;
    private final HospitalService hospitalService;
    private final BloodRequestService bloodRequestService;
    private final MatchingService matchingService;
    private final BloodBankService bloodBankService;

    private final ServiceHealthRegistry registry;

    public HealthChecker(
            DonorService donorService,
            HospitalService hospitalService,
            BloodRequestService bloodRequestService,
            MatchingService matchingService,
            BloodBankService bloodBankService,
            ServiceHealthRegistry registry) {

        this.donorService = donorService;
        this.hospitalService = hospitalService;
        this.bloodRequestService = bloodRequestService;
        this.matchingService = matchingService;
        this.bloodBankService = bloodBankService;
        this.registry = registry;
    }

    @Scheduled(fixedRate = 10000) // every 10 sec
    public void checkServices() {

        check("DonorService", () -> donorService.getAllDonors());
        check("HospitalService", () -> hospitalService.getAll());
        check("BloodRequestService", () -> bloodRequestService.getAllRequests());
        check("BloodBankService", () -> bloodBankService.getAllBloodBanks());

        // MatchingService depends on donorService
        check("MatchingService", () -> donorService.getAllDonors());
    }

    private void check(String name, Runnable task) {
        try {
            task.run();
            registry.markUp(name);
            System.out.println(name + " ✅ UP");
        } catch (Exception e) {
            registry.markDown(name);
            System.out.println(name + " ❌ DOWN");
        }
    }
}