package com.example.bloodmatch.bloodbank;

import com.example.bloodmatch.bloodbank.BloodBank;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BloodBankRepository extends MongoRepository<BloodBank, String> {
    BloodBank findByEmail(String email);
}
