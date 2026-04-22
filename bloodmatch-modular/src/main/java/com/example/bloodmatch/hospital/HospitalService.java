package com.example.bloodmatch.hospital;

import com.example.bloodmatch.hospital.Hospital;
import com.example.bloodmatch.hospital.HospitalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HospitalService {

    private static final Logger logger = LoggerFactory.getLogger(HospitalService.class);

    private final HospitalRepository hospitalRepository;

    public HospitalService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }

    public Hospital saveHospital(Hospital hospital) {

        logger.info("Creating hospital: email={}, name={}",
                hospital.getEmail(), hospital.getName());

        Hospital saved = hospitalRepository.save(hospital);

        logger.info("Hospital created successfully with id={}", saved.getId());

        return saved;
    }

    public Hospital findByEmail(String email) {
        logger.info("Searching hospital by email={}", email);
        return hospitalRepository.findByEmail(email);
    }

    public Hospital findByName(String name) {
        logger.info("Searching hospital by name={}", name);
        return hospitalRepository.findByName(name);
    }

    public Hospital updateHospital(String id, Hospital hospital) {

        logger.info("Updating hospital with id={}", id);

        hospital.setId(id);
        Hospital updated = hospitalRepository.save(hospital);

        logger.info("Hospital updated successfully with id={}", id);

        return updated;
    }

    public Hospital updateAddress(String id, String address, Double latitude, Double longitude) {

        logger.info("Updating hospital address: id={}, lat={}, lon={}",
                id, latitude, longitude);

        return hospitalRepository.findById(id).map(h -> {

            if (address != null) h.setAddress(address);
            if (latitude != null) h.setLatitude(latitude);
            if (longitude != null) h.setLongitude(longitude);

            Hospital saved = hospitalRepository.save(h);

            logger.info("Hospital address updated successfully for id={}", id);

            return saved;

        }).orElseGet(() -> {
            logger.warn("Hospital not found for address update, id={}", id);
            return null;
        });
    }

    public List<Hospital> getAll() {

        logger.info("Fetching all hospitals");

        List<Hospital> hospitals = hospitalRepository.findAll();

        logger.info("Total hospitals fetched: {}", hospitals.size());

        return hospitals;
    }
}