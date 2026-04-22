package com.example.bloodmatch.bloodbank;

import com.example.bloodmatch.donor.EmergencyDonorAlertService;


import com.example.bloodmatch.bloodbank.BloodPacket;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodPacketRepository extends MongoRepository<BloodPacket, String> {

    /** All packets belonging to a specific blood bank */
    List<BloodPacket> findByBloodBankId(String bloodBankId);

    /** Packets filtered by blood bank and blood group */
    List<BloodPacket> findByBloodBankIdAndBloodGroup(String bloodBankId, String bloodGroup);

    /** Packets filtered by blood bank and status (AVAILABLE / DONATED / EXPIRED) */
    List<BloodPacket> findByBloodBankIdAndStatus(String bloodBankId, String status);

    /** Packets filtered by blood bank, blood group, and status */
    List<BloodPacket> findByBloodBankIdAndBloodGroupAndStatus(String bloodBankId, String bloodGroup, String status);

    /**
     * Count of AVAILABLE packets of a given blood group across ALL blood banks.
     * Used by EmergencyDonorAlertService to detect a network-wide zero-stock condition.
     */
    long countByBloodGroupAndStatus(String bloodGroup, String status);
}

