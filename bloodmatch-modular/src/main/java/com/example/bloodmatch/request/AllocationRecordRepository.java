package com.example.bloodmatch.request;

import com.example.bloodmatch.request.AllocationRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AllocationRecordRepository extends MongoRepository<AllocationRecord, String> {
    List<AllocationRecord> findByBloodBankId(String bloodBankId);
}
