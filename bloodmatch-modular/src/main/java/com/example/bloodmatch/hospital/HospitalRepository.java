package com.example.bloodmatch.hospital;

import com.example.bloodmatch.hospital.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HospitalRepository extends MongoRepository<Hospital, String> {
    Hospital findByEmail(String email);
    Hospital findByName(String name);
}
