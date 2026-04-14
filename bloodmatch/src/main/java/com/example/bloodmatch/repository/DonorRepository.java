package com.example.bloodmatch.repository;

import com.example.bloodmatch.model.Donor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DonorRepository extends MongoRepository<Donor, String> {
	Donor findByEmail(String email);
}