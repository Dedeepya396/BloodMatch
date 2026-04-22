package com.example.bloodmatch.donor;

import com.example.bloodmatch.donor.Donor;
import com.example.bloodmatch.donor.AddressUpdateRequest;
import com.example.bloodmatch.donor.DonationRequest;
import com.example.bloodmatch.request.BloodRequestService;
import com.example.bloodmatch.donor.DonorService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RestController
@RequestMapping("/api/donors")
public class DonorController {

    private final DonorService donorService;
    private static final Logger logger = LoggerFactory.getLogger(BloodRequestService.class);

    public DonorController(DonorService donorService) {
        this.donorService = donorService;
    }

    // 1. Add donor
    @PostMapping
    public Donor addDonor(@RequestBody Donor donor) {
        logger.info("Received a request to add a donor with email: {}", donor.getEmail());
        return donorService.saveDonor(donor);
    }

    // 2. Get all donors
    @GetMapping
    public List<Donor> getAllDonors() {
        logger.info("Received a  request to GET all donors");
        return donorService.getAllDonors();
    }

    @GetMapping("/by-email")
    public ResponseEntity<?> getByEmail(@RequestParam String email) {
        logger.info("Received a request to get donors information through email:{} .", email);
        return ResponseEntity.ok(donorService.findByEmail(email));
    }

    @PutMapping("/{id}")
    public Donor updateDonor(@PathVariable String id, @RequestBody Donor donor) {
        logger.info("Received a request to update a donor with id: {}", id);
        return donorService.updateDonor(id, donor);
    }

    @PutMapping("/{id}/address")
    public ResponseEntity<?> updateAddress(@PathVariable String id,
            @RequestBody AddressUpdateRequest req) {
        logger.info("Received a request to update address of donor with id: {}", id);
        Donor updated = donorService.updateAddress(id, req.getAddress(), req.getLatitude(), req.getLongitude());
        if (updated == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }
    @PostMapping("/{id}/donate")
    public ResponseEntity<?> donate(@PathVariable String id, @RequestBody DonationRequest req) {
        logger.info("Received a request for blood donation from donor with id: {}", id);
        try {
            Donor updated = donorService.donateBlood(id, req);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}