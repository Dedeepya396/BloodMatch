package com.example.bloodmatch.bloodbank;

import com.example.bloodmatch.bloodbank.BankRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankRequestRepository extends MongoRepository<BankRequest, String> {
    List<BankRequest> findByBankIdAndStatus(String bankId, String status);
    List<BankRequest> findByBloodRequestId(String bloodRequestId);
}
