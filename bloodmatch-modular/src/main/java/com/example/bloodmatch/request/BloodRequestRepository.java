package com.example.bloodmatch.request;

import com.example.bloodmatch.request.BloodRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BloodRequestRepository extends MongoRepository<BloodRequest, String> {
}