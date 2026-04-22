package com.example.bloodmatch.donor;

import com.example.bloodmatch.donor.Donor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonorRepository extends MongoRepository<Donor, String> {
	Donor findByEmail(String email);
	List<Donor> findByBloodGroup(String bloodGroup);
	List<Donor> findByBloodGroupIn(List<String> bloodGroups);
}