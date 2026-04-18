package com.example.bloodmatch.service;

import com.example.bloodmatch.model.Hospital;
import com.example.bloodmatch.repository.HospitalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HospitalService {

    private final HospitalRepository hospitalRepository;

    public HospitalService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }

    public Hospital saveHospital(Hospital hospital) {
        return hospitalRepository.save(hospital);
    }

    public Hospital findByEmail(String email) {
        return hospitalRepository.findByEmail(email);
    }

    public Hospital updateHospital(String id, Hospital hospital) {
        hospital.setId(id);
        return hospitalRepository.save(hospital);
    }

    public Hospital updateAddress(String id, String address, Double latitude, Double longitude) {
        return hospitalRepository.findById(id).map(h -> {
            if (address != null) h.setAddress(address);
            if (latitude != null) h.setLatitude(latitude);
            if (longitude != null) h.setLongitude(longitude);
            return hospitalRepository.save(h);
        }).orElse(null);
    }

    public List<Hospital> getAll() {
        return hospitalRepository.findAll();
    }
}
