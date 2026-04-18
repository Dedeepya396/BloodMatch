package com.example.bloodmatch.service;

import com.example.bloodmatch.model.Donor;
import com.example.bloodmatch.repository.DonorRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class DonorService {

    private final DonorRepository donorRepository;
    private static final Logger logger = LoggerFactory.getLogger(DonorService.class);

    public DonorService(DonorRepository donorRepository) {
        this.donorRepository = donorRepository;
    }

    // Save donor
    public Donor saveDonor(Donor donor) {
        logger.info("Saving donor: email={}, bloodGroup={}",
                donor.getEmail(), donor.getBloodGroup());

        Donor saved = donorRepository.save(donor);
        logger.info("Donor saved successfully with id={}", saved.getId());
        return saved;
    }

    // Get all donors
    public List<Donor> getAllDonors() {

        logger.info("Fetching all donors");

        List<Donor> donors = donorRepository.findAll();

        logger.info("Total donors fetched: {}", donors.size());
        return donors;
    }

    public Donor findByEmail(String email) {
        logger.info("Searching donor by email={}", email);
        Donor donor = donorRepository.findByEmail(email);
        if (donor == null) {
            logger.warn("No donor found with email={}", email);
        } else
            logger.info("Donor found for email={}", email);

        return donor;
    }

    public Donor updateDonor(String id, Donor donor) {
        logger.info("Updating donor with id={}", id);

        donor.setId(id);
        Donor updated = donorRepository.save(donor);

        logger.info("Donor updated successfully with id={}", id);
        return updated;
    }

    public Donor updateAddress(String id, String address, Double latitude, Double longitude) {
        logger.info("Updating address for donorId={}, lat={}, lon={}",
                id, latitude, longitude);

        return donorRepository.findById(id).map(d -> {

            if (address != null)
                d.setAddress(address);
            if (latitude != null)
                d.setLatitude(latitude);
            if (longitude != null)
                d.setLongitude(longitude);

            Donor saved = donorRepository.save(d);

            logger.info("Address updated successfully for donorId={}", id);
            return saved;

        }).orElseGet(() -> {
            logger.warn("Donor not found for address update, id={}", id);
            return null;
        });

    }
}