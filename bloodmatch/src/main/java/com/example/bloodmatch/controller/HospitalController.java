package com.example.bloodmatch.controller;

import com.example.bloodmatch.model.Hospital;
import com.example.bloodmatch.service.HospitalService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {

    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @PostMapping
    public Hospital createHospital(@RequestBody Hospital hospital) {
        return hospitalService.saveHospital(hospital);
    }

    @GetMapping("/by-email")
    public ResponseEntity<?> getByEmail(@RequestParam String email) {
        return ResponseEntity.ok(hospitalService.findByEmail(email));
    }

    @PutMapping("/{id}")
    public Hospital updateHospital(@PathVariable String id, @RequestBody Hospital hospital) {
        return hospitalService.updateHospital(id, hospital);
    }

    @PutMapping("/{id}/address")
    public ResponseEntity<?> updateAddress(@PathVariable String id, @RequestBody com.example.bloodmatch.dto.AddressUpdateRequest req) {
        Hospital updated = hospitalService.updateAddress(id, req.getAddress(), req.getLatitude(), req.getLongitude());
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public List<Hospital> getAll() {
        return hospitalService.getAll();
    }
}
