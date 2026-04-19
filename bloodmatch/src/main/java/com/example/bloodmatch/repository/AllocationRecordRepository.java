package com.example.bloodmatch.repository;

import com.example.bloodmatch.model.AllocationRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AllocationRecordRepository extends MongoRepository<AllocationRecord, String> {
    List<AllocationRecord> findByBloodBankId(String bloodBankId);
}
