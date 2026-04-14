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

    @GetMapping
    public List<Hospital> getAll() {
        return hospitalService.getAll();
    }
}
