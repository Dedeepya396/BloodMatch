package com.example.bloodmatch.controller;

import com.example.bloodmatch.dto.BloodPacketRequest;
import com.example.bloodmatch.model.BloodPacket;
import com.example.bloodmatch.repository.BloodPacketRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import com.example.bloodmatch.dto.AllocationRequest;
import com.example.bloodmatch.model.AllocationRecord;

/**
 * REST controller for blood packet management within a Blood Bank.
 * Base URL: /api/bloodbank
 */
@RestController
@RequestMapping("/api/bloodbank")
public class BloodBankController {

    private final BloodPacketRepository packetRepository;
    private final com.example.bloodmatch.repository.BloodBankRepository bloodBankRepository;
    private final com.example.bloodmatch.repository.AllocationRecordRepository allocationRecordRepository;

    public BloodBankController(BloodPacketRepository packetRepository, 
                               com.example.bloodmatch.repository.BloodBankRepository bloodBankRepository,
                               com.example.bloodmatch.repository.AllocationRecordRepository allocationRecordRepository) {
        this.packetRepository = packetRepository;
        this.bloodBankRepository = bloodBankRepository;
        this.allocationRecordRepository = allocationRecordRepository;
    }

    @GetMapping
    public ResponseEntity<List<com.example.bloodmatch.model.BloodBank>> getAllBloodBanks() {
        return ResponseEntity.ok(bloodBankRepository.findAll());
    }

    @GetMapping("/by-email")
    public ResponseEntity<?> getBloodBankByEmail(@RequestParam String email) {
        com.example.bloodmatch.model.BloodBank bb = bloodBankRepository.findByEmail(email);
        if (bb == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(bb);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBloodBank(@PathVariable String id, @RequestBody com.example.bloodmatch.model.BloodBank update) {
        Optional<com.example.bloodmatch.model.BloodBank> opt = bloodBankRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        com.example.bloodmatch.model.BloodBank bb = opt.get();
        bb.setName(update.getName());
        bb.setAddress(update.getAddress());
        bb.setContactNumber(update.getContactNumber());
        bloodBankRepository.save(bb);
        return ResponseEntity.ok(bb);
    }

    @PutMapping("/{id}/address")
    public ResponseEntity<?> updateBloodBankLocation(@PathVariable String id, @RequestBody com.example.bloodmatch.model.BloodBank update) {
        Optional<com.example.bloodmatch.model.BloodBank> opt = bloodBankRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        com.example.bloodmatch.model.BloodBank bb = opt.get();
        bb.setAddress(update.getAddress());
        bb.setLatitude(update.getLatitude());
        bb.setLongitude(update.getLongitude());
        bloodBankRepository.save(bb);
        return ResponseEntity.ok(bb);
    }

    // ----------------------------------------------------------------
    // GET  /packets/{bankId}           → all packets for this bank
    // ----------------------------------------------------------------
    @GetMapping("/packets/{bankId}")
    public ResponseEntity<List<BloodPacket>> getAllPackets(@PathVariable String bankId) {
        List<BloodPacket> packets = packetRepository.findByBloodBankId(bankId);
        // Refresh expired status before returning
        packets = refreshExpiredStatus(packets);
        return ResponseEntity.ok(packets);
    }

    // ----------------------------------------------------------------
    // GET  /packets/{bankId}/group/{bg} → packets filtered by blood group
    // ----------------------------------------------------------------
    @GetMapping("/packets/{bankId}/group/{bg}")
    public ResponseEntity<List<BloodPacket>> getPacketsByGroup(
            @PathVariable String bankId,
            @PathVariable String bg) {
        List<BloodPacket> packets = packetRepository.findByBloodBankIdAndBloodGroup(bankId, bg);
        packets = refreshExpiredStatus(packets);
        return ResponseEntity.ok(packets);
    }

    // ----------------------------------------------------------------
    // GET  /packets/{bankId}/expired   → packets that are expired
    // ----------------------------------------------------------------
    @GetMapping("/packets/{bankId}/expired")
    public ResponseEntity<List<BloodPacket>> getExpiredPackets(@PathVariable String bankId) {
        List<BloodPacket> all = packetRepository.findByBloodBankId(bankId);
        all = refreshExpiredStatus(all);
        List<BloodPacket> expired = all.stream()
                .filter(p -> "EXPIRED".equals(p.getStatus()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(expired);
    }

    // ----------------------------------------------------------------
    // POST /packets/{bankId}           → add a new blood packet
    // ----------------------------------------------------------------
    @PostMapping("/packets/{bankId}")
    public ResponseEntity<?> addPacket(@PathVariable String bankId,
                                        @RequestBody BloodPacketRequest req) {
        if (req.getBloodGroup() == null || req.getCollectedDate() == null || req.getUnits() <= 0) {
            return ResponseEntity.badRequest().body("Blood group, collected date, and units (>0) are required");
        }
        LocalDate collected;
        try {
            collected = LocalDate.parse(req.getCollectedDate());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid date format — use yyyy-MM-dd");
        }
        BloodPacket packet = new BloodPacket(bankId, req.getBloodGroup(), collected, req.getUnits());
        BloodPacket saved = packetRepository.save(packet);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ----------------------------------------------------------------
    // PATCH /packets/{packetId}/donate → mark packet as DONATED
    // ----------------------------------------------------------------
    @PatchMapping("/packets/{packetId}/donate")
    public ResponseEntity<?> markDonated(@PathVariable String packetId) {
        Optional<BloodPacket> opt = packetRepository.findById(packetId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        BloodPacket p = opt.get();
        p.setStatus("DONATED");
        packetRepository.save(p);
        return ResponseEntity.ok(p);
    }

    // ----------------------------------------------------------------
    // PATCH /packets/{packetId}/expire → mark packet as EXPIRED
    // ----------------------------------------------------------------
    @PatchMapping("/packets/{packetId}/expire")
    public ResponseEntity<?> markExpired(@PathVariable String packetId) {
        Optional<BloodPacket> opt = packetRepository.findById(packetId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        BloodPacket p = opt.get();
        p.setStatus("EXPIRED");
        packetRepository.save(p);
        return ResponseEntity.ok(p);
    }

    // ----------------------------------------------------------------
    // DELETE /packets/{packetId}       → permanently remove a packet
    // ----------------------------------------------------------------
    @DeleteMapping("/packets/{packetId}")
    public ResponseEntity<?> deletePacket(@PathVariable String packetId) {
        if (!packetRepository.existsById(packetId)) {
            return ResponseEntity.notFound().build();
        }
        packetRepository.deleteById(packetId);
        return ResponseEntity.ok("Packet deleted");
    }

    // ----------------------------------------------------------------
    // POST /packets/{bankId}/allocate       → fulfill a blood request
    // ----------------------------------------------------------------
    @PostMapping("/packets/{bankId}/allocate")
    public ResponseEntity<?> allocateBlood(@PathVariable String bankId, @RequestBody AllocationRequest req) {
        List<BloodPacket> availablePackets = packetRepository.findByBloodBankId(bankId).stream()
                .filter(p -> "AVAILABLE".equals(p.getStatus()) && p.getBloodGroup().equals(req.getBloodGroup()))
                .sorted(Comparator.comparing(BloodPacket::getExpiryDate))
                .collect(Collectors.toList());

        int required = req.getUnits();
        int currentInventory = availablePackets.stream().mapToInt(BloodPacket::getUnits).sum();

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

        return ResponseEntity.ok(response);
    }

    // ----------------------------------------------------------------
    // GET /packets/{bankId}/donated       → return allocation history
    // ----------------------------------------------------------------
    @GetMapping("/packets/{bankId}/donated")
    public ResponseEntity<List<AllocationRecord>> getDonatedPackets(@PathVariable String bankId) {
        List<AllocationRecord> records = allocationRecordRepository.findByBloodBankId(bankId);
        return ResponseEntity.ok(records);
    }

    // ----------------------------------------------------------------
    // Internal helper: auto-expire packets whose expiry date has passed
    // ----------------------------------------------------------------
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
