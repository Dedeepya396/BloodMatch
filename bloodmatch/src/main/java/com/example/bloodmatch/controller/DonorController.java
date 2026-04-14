package com.example.bloodmatch.controller;

import com.example.bloodmatch.model.Donor;
import com.example.bloodmatch.service.DonorService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/donors")
public class DonorController {

    private final DonorService donorService;

    public DonorController(DonorService donorService) {
        this.donorService = donorService;
    }

    // 1. Add donor
    @PostMapping
    public Donor addDonor(@RequestBody Donor donor) {
        return donorService.saveDonor(donor);
    }

    // 2. Get all donors
    @GetMapping
    public List<Donor> getAllDonors() {
        return donorService.getAllDonors();
    }

    @GetMapping("/by-email")
    public ResponseEntity<?> getByEmail(@RequestParam String email) {
        return ResponseEntity.ok(donorService.findByEmail(email));
    }

    @PutMapping("/{id}")
    public Donor updateDonor(@PathVariable String id, @RequestBody Donor donor) {
        return donorService.updateDonor(id, donor);
    }
}