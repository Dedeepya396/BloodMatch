package com.example.bloodmatch.bloodbank;

import com.example.bloodmatch.request.AllocationRequest;
import com.example.bloodmatch.request.BloodPacketRequest;
import com.example.bloodmatch.request.AllocationRecord;
import com.example.bloodmatch.bloodbank.BloodBank;
import com.example.bloodmatch.bloodbank.BloodPacket;
import com.example.bloodmatch.request.AllocationRecordRepository;
import com.example.bloodmatch.bloodbank.BloodBankRepository;
import com.example.bloodmatch.bloodbank.BloodPacketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing blood bank inventory and details.
 * Implements core logic like FEFO (First Expiry First Out) for blood allocation.
 */
@Service
public class BloodBankService {

    private final BloodPacketRepository packetRepository;
    private final BloodBankRepository bloodBankRepository;
    private final AllocationRecordRepository allocationRecordRepository;

    public BloodBankService(BloodPacketRepository packetRepository,
                            BloodBankRepository bloodBankRepository,
                            AllocationRecordRepository allocationRecordRepository) {
        this.packetRepository = packetRepository;
        this.bloodBankRepository = bloodBankRepository;
        this.allocationRecordRepository = allocationRecordRepository;
    }

    public List<BloodBank> getAllBloodBanks() {
        return bloodBankRepository.findAll();
    }

    public Optional<BloodBank> getById(String id) {
        return bloodBankRepository.findById(id);
    }

    public BloodBank getByEmail(String email) {
        return bloodBankRepository.findByEmail(email);
    }

    public BloodBank save(BloodBank bloodBank) {
        return bloodBankRepository.save(bloodBank);
    }

    public List<BloodPacket> getAllPackets(String bankId) {
        List<BloodPacket> packets = packetRepository.findByBloodBankId(bankId);
        return refreshExpiredStatus(packets);
    }

    public List<BloodPacket> getPacketsByGroup(String bankId, String bg) {
        List<BloodPacket> packets = packetRepository.findByBloodBankIdAndBloodGroup(bankId, bg);
        return refreshExpiredStatus(packets);
    }

    public List<BloodPacket> getExpiredPackets(String bankId) {
        List<BloodPacket> all = packetRepository.findByBloodBankId(bankId);
        all = refreshExpiredStatus(all);
        return all.stream()
                .filter(p -> "EXPIRED".equals(p.getStatus()))
                .collect(Collectors.toList());
    }

    public BloodPacket addPacket(String bankId, BloodPacketRequest req) {
        LocalDate collected = LocalDate.parse(req.getCollectedDate());
        BloodPacket packet = new BloodPacket(bankId, req.getBloodGroup(), collected, req.getUnits());
        return packetRepository.save(packet);
    }

    public Optional<BloodPacket> markDonated(String packetId) {
        return packetRepository.findById(packetId).map(p -> {
            p.setStatus("DONATED");
            return packetRepository.save(p);
        });
    }

    public Optional<BloodPacket> markExpired(String packetId) {
        return packetRepository.findById(packetId).map(p -> {
            p.setStatus("EXPIRED");
            return packetRepository.save(p);
        });
    }

    public boolean deletePacket(String packetId) {
        if (!packetRepository.existsById(packetId)) {
            return false;
        }
        packetRepository.deleteById(packetId);
        return true;
    }

    /**
     * Allocates blood packets based on FEFO (First Expiry First Out).
     */
    public Map<String, Object> allocateBlood(String bankId, AllocationRequest req) {
        List<BloodPacket> availablePackets = packetRepository.findByBloodBankId(bankId).stream()
                .filter(p -> "AVAILABLE".equals(p.getStatus()) && p.getBloodGroup().equals(req.getBloodGroup()))
                .sorted(Comparator.comparing(BloodPacket::getExpiryDate))
                .collect(Collectors.toList());

        int required = req.getUnits();
        List<AllocationRecord> records = new ArrayList<>();
        int allocatedCount = 0;
        int stillNeeded = required;

        for (BloodPacket packet : availablePackets) {
            if (stillNeeded <= 0) break;

            int unitsToTake = Math.min(packet.getUnits(), stillNeeded);
            packet.setUnits(packet.getUnits() - unitsToTake);
            stillNeeded -= unitsToTake;
            allocatedCount += unitsToTake;

            if (packet.getUnits() == 0) {
                packet.setStatus("DONATED");
            }

            packetRepository.save(packet);

            AllocationRecord record = new AllocationRecord(bankId, packet.getBloodGroup(), packet.getId(), unitsToTake);
            allocationRecordRepository.save(record);
            records.add(record);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("records", records);
        response.put("allocated", allocatedCount);
        response.put("requested", required);
        response.put("shortfall", required - allocatedCount);

        return response;
    }

    public List<AllocationRecord> getDonationHistory(String bankId) {
        return allocationRecordRepository.findByBloodBankId(bankId);
    }

    /**
     * Returns total AVAILABLE units for a given blood group at a bank.
     */
    public int getAvailableUnits(String bankId, String bloodGroup) {
        List<BloodPacket> packets = packetRepository.findByBloodBankIdAndBloodGroupAndStatus(bankId, bloodGroup, "AVAILABLE");
        packets = refreshExpiredStatus(packets);
        return packets.stream().mapToInt(BloodPacket::getUnits).sum();
    }

    private List<BloodPacket> refreshExpiredStatus(List<BloodPacket> packets) {
        LocalDate today = LocalDate.now();
        packets.forEach(p -> {
            if ("AVAILABLE".equals(p.getStatus()) && p.getExpiryDate() != null
                    && p.getExpiryDate().isBefore(today)) {
                p.setStatus("EXPIRED");
                packetRepository.save(p);
            }
        });
        return packets;
    }
}
