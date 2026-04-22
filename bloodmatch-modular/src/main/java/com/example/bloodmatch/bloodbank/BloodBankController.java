package com.example.bloodmatch.bloodbank;

import com.example.bloodmatch.request.BloodPacketRequest;
import com.example.bloodmatch.bloodbank.BloodPacket;
import com.example.bloodmatch.bloodbank.BloodBankService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import com.example.bloodmatch.request.AllocationRequest;
import com.example.bloodmatch.request.AllocationRecord;

/**
 * REST controller for blood packet management within a Blood Bank.
 * Base URL: /api/bloodbank
 */
@RestController
@RequestMapping("/api/bloodbank")
public class BloodBankController {

    private final BloodBankService bloodBankService;

    public BloodBankController(BloodBankService bloodBankService) {
        this.bloodBankService = bloodBankService;
    }

    @GetMapping
    public ResponseEntity<List<BloodBank>> getAllBloodBanks() {
        return ResponseEntity.ok(bloodBankService.getAllBloodBanks());
    }

    @GetMapping("/by-email")
    public ResponseEntity<?> getBloodBankByEmail(@RequestParam String email) {
        BloodBank bb = bloodBankService.getByEmail(email);
        if (bb == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(bb);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBloodBank(@PathVariable String id, @RequestBody BloodBank update) {
        Optional<BloodBank> opt = bloodBankService.getById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        BloodBank bb = opt.get();
        bb.setName(update.getName());
        bb.setAddress(update.getAddress());
        bb.setContactNumber(update.getContactNumber());
        bloodBankService.save(bb);
        return ResponseEntity.ok(bb);
    }

    @PutMapping("/{id}/address")
    public ResponseEntity<?> updateBloodBankLocation(@PathVariable String id, @RequestBody BloodBank update) {
        Optional<BloodBank> opt = bloodBankService.getById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        BloodBank bb = opt.get();
        bb.setAddress(update.getAddress());
        bb.setLatitude(update.getLatitude());
        bb.setLongitude(update.getLongitude());
        bloodBankService.save(bb);
        return ResponseEntity.ok(bb);
    }

    // ----------------------------------------------------------------
    // GET  /packets/{bankId}           → all packets for this bank
    // ----------------------------------------------------------------
    @GetMapping("/packets/{bankId}")
    public ResponseEntity<List<BloodPacket>> getAllPackets(@PathVariable String bankId) {
        return ResponseEntity.ok(bloodBankService.getAllPackets(bankId));
    }

    // ----------------------------------------------------------------
    // GET  /packets/{bankId}/group/{bg} → packets filtered by blood group
    // ----------------------------------------------------------------
    @GetMapping("/packets/{bankId}/group/{bg}")
    public ResponseEntity<List<BloodPacket>> getPacketsByGroup(
            @PathVariable String bankId,
            @PathVariable String bg) {
        return ResponseEntity.ok(bloodBankService.getPacketsByGroup(bankId, bg));
    }

    // ----------------------------------------------------------------
    // GET  /packets/{bankId}/expired   → packets that are expired
    // ----------------------------------------------------------------
    @GetMapping("/packets/{bankId}/expired")
    public ResponseEntity<List<BloodPacket>> getExpiredPackets(@PathVariable String bankId) {
        return ResponseEntity.ok(bloodBankService.getExpiredPackets(bankId));
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
        try {
            BloodPacket saved = bloodBankService.addPacket(bankId, req);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid date format or data — use yyyy-MM-dd");
        }
    }

    // ----------------------------------------------------------------
    // PATCH /packets/{packetId}/donate → mark packet as DONATED
    // ----------------------------------------------------------------
    @PatchMapping("/packets/{packetId}/donate")
    public ResponseEntity<?> markDonated(@PathVariable String packetId) {
        return bloodBankService.markDonated(packetId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ----------------------------------------------------------------
    // PATCH /packets/{packetId}/expire → mark packet as EXPIRED
    // ----------------------------------------------------------------
    @PatchMapping("/packets/{packetId}/expire")
    public ResponseEntity<?> markExpired(@PathVariable String packetId) {
        return bloodBankService.markExpired(packetId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ----------------------------------------------------------------
    // DELETE /packets/{packetId}       → permanently remove a packet
    // ----------------------------------------------------------------
    @DeleteMapping("/packets/{packetId}")
    public ResponseEntity<?> deletePacket(@PathVariable String packetId) {
        if (bloodBankService.deletePacket(packetId)) {
            return ResponseEntity.ok("Packet deleted");
        }
        return ResponseEntity.notFound().build();
    }

    // ----------------------------------------------------------------
    // POST /packets/{bankId}/allocate       → fulfill a blood request
    // ----------------------------------------------------------------
    @PostMapping("/packets/{bankId}/allocate")
    public ResponseEntity<?> allocateBlood(@PathVariable String bankId, @RequestBody AllocationRequest req) {
        Map<String, Object> result = bloodBankService.allocateBlood(bankId, req);
        return ResponseEntity.ok(result);
    }

    // ----------------------------------------------------------------
    // GET /packets/{bankId}/donated       → return allocation history
    // ----------------------------------------------------------------
    @GetMapping("/packets/{bankId}/donated")
    public ResponseEntity<List<AllocationRecord>> getDonatedPackets(@PathVariable String bankId) {
        return ResponseEntity.ok(bloodBankService.getDonationHistory(bankId));
    }
}
