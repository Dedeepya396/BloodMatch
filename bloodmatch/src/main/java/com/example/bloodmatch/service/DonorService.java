package com.example.bloodmatch.service;

import com.example.bloodmatch.model.Donor;
import com.example.bloodmatch.repository.DonorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DonorService {

    private final DonorRepository donorRepository;

    public DonorService(DonorRepository donorRepository) {
        this.donorRepository = donorRepository;
    }

    // Save donor
    public Donor saveDonor(Donor donor) {
        return donorRepository.save(donor);
    }

    // Get all donors
    public List<Donor> getAllDonors() {
        return donorRepository.findAll();
    }

    public Donor findByEmail(String email) {
        return donorRepository.findByEmail(email);
    }

    public Donor updateDonor(String id, Donor donor) {
        donor.setId(id);
        return donorRepository.save(donor);
    }
}