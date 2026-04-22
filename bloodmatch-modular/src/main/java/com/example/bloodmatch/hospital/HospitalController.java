package com.example.bloodmatch.hospital;

import com.example.bloodmatch.donor.AddressUpdateRequest;
import com.example.bloodmatch.hospital.Hospital;
import com.example.bloodmatch.hospital.HospitalService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {

    private static final Logger logger = LoggerFactory.getLogger(HospitalController.class);

    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @PostMapping
    public Hospital createHospital(@RequestBody Hospital hospital) {
        logger.info("Received request to create hospital: email={}, name={}",
                hospital.getEmail(), hospital.getName());

        Hospital saved = hospitalService.saveHospital(hospital);

        logger.info("Hospital created successfully with id={}", saved.getId());
        return saved;
    }

    @GetMapping("/by-email")
    public ResponseEntity<?> getByEmail(@RequestParam String email) {
        logger.info("Fetching hospital by email={}", email);

        Object result = hospitalService.findByEmail(email);

        if (result == null) {
            logger.warn("No hospital found for email={}", email);
        } else {
            logger.info("Hospital found for email={}", email);
        }

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public Hospital updateHospital(@PathVariable String id, @RequestBody Hospital hospital) {
        logger.info("Updating hospital with id={}", id);

        Hospital updated = hospitalService.updateHospital(id, hospital);

        logger.info("Hospital updated successfully with id={}", id);
        return updated;
    }

    @PutMapping("/{id}/address")
    public ResponseEntity<?> updateAddress(
            @PathVariable String id,
            @RequestBody AddressUpdateRequest req) {

        logger.info("Updating address for hospitalId={}, lat={}, lon={}",
                id, req.getLatitude(), req.getLongitude());

        Hospital updated = hospitalService.updateAddress(
                id, req.getAddress(), req.getLatitude(), req.getLongitude());

        if (updated == null) {
            logger.warn("Hospital not found for address update, id={}", id);
            return ResponseEntity.notFound().build();
        }

        logger.info("Address updated successfully for hospitalId={}", id);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public List<Hospital> getAll() {
        logger.info("Fetching all hospitals");

        List<Hospital> hospitals = hospitalService.getAll();

        logger.info("Fetched {} hospitals", hospitals.size());
        return hospitals;
    }
}