package com.example.bloodmatch.repository;

import com.example.bloodmatch.model.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HospitalRepository extends MongoRepository<Hospital, String> {
    Hospital findByEmail(String email);
    Hospital findByName(String name);
}
