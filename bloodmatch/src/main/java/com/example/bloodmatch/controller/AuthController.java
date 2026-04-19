package com.example.bloodmatch.controller;

import com.example.bloodmatch.model.BloodBank;
import com.example.bloodmatch.model.Donor;
import com.example.bloodmatch.model.Hospital;
import com.example.bloodmatch.model.factory.UserFactory;
import com.example.bloodmatch.repository.BloodBankRepository;
import com.example.bloodmatch.repository.DonorRepository;
import com.example.bloodmatch.repository.HospitalRepository;
import com.example.bloodmatch.service.BloodRequestService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.bloodmatch.dto.*;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final DonorRepository donorRepository;
    private final HospitalRepository hospitalRepository;
    private final BloodBankRepository bloodBankRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final Logger logger = LoggerFactory.getLogger(BloodRequestService.class);

    public AuthController(DonorRepository donorRepository,
                          HospitalRepository hospitalRepository,
                          BloodBankRepository bloodBankRepository) {
        this.donorRepository = donorRepository;
        this.hospitalRepository = hospitalRepository;
        this.bloodBankRepository = bloodBankRepository;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        String role = req.getRole();
        String email = req.getEmail();
        logger.info("Received signup request: role={}, email:{}", role, email);
        if (email == null || req.getPassword() == null) {
            if (email == null)
                logger.error("Email is NULL");
            else
                logger.error("Password is NULL");
            return ResponseEntity.badRequest().body("Email and password required");
        }
        // if user is donor
        if ("DONOR".equalsIgnoreCase(role)) {
            // if email already exists
            if (donorRepository.findByEmail(email) != null) {
                logger.error("Donor with email={} already exists", email);
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered as donor");
            }
            String hash = passwordEncoder.encode(req.getPassword());
            // if last donation date is not null that is parsed else it is kept as null
            LocalDate lastDonation = req.getLastDonationDate() != null ? LocalDate.parse(req.getLastDonationDate())
                    : null;
            // create a donor through user factory
            Donor donor = UserFactory.createDonor(req.getName(), email, hash, req.getBloodGroup(), req.getLatitude(),
                    req.getLongitude(), req.isAvailable(), lastDonation);
            // save it to db
            Donor saved = donorRepository.save(donor);
            // sending status code 200 with user id, role, name, email
            logger.info("New donor with email={} is registered", email);
            return ResponseEntity.ok(new AuthResponse(saved.getId(), "DONOR", saved.getName(), email));

        }
        // if user is a hospital
        else if ("HOSPITAL".equalsIgnoreCase(role)) {
            if (hospitalRepository.findByEmail(email) != null) {
                logger.error("Hospital with email={} already exists", email);
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered as hospital");
            }
            String hash = passwordEncoder.encode(req.getPassword());
            // directly create a hospital
            Hospital hospital = UserFactory.createHospital(req.getName(), email, hash, req.getAddress(),
                    req.getLatitude(), req.getLongitude(), req.getContactNumber());
            Hospital saved = hospitalRepository.save(hospital);
            logger.info("New hospital with email={} is registered", email);
            return ResponseEntity.ok(new AuthResponse(saved.getId(), "HOSPITAL", saved.getName(), email));
        }
        // if user is a blood bank
        else if ("BLOOD_BANK".equalsIgnoreCase(role)) {
            if (bloodBankRepository.findByEmail(email) != null) {
                logger.error("Blood Bank with email={} already exists", email);
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered as blood bank");
            }
            String hash = passwordEncoder.encode(req.getPassword());
            BloodBank bloodBank = UserFactory.createBloodBank(req.getName(), email, hash,
                    req.getAddress(), req.getContactNumber(), req.getLatitude(), req.getLongitude());
            BloodBank saved = bloodBankRepository.save(bloodBank);
            logger.info("New blood bank with email={} is registered", email);
            return ResponseEntity.ok(new AuthResponse(saved.getId(), "BLOOD_BANK", saved.getName(), email));
        }
        // unknown role
        logger.error("User with email={} has selected unknown role", email);
        return ResponseEntity.badRequest().body("Unknown role");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        String email = req.getEmail();
        String pw = req.getPassword();
        logger.info("Received login request: email:{}", email);
        if (email == null || pw == null) {
            if (email == null)
                logger.error("Email is NULL");
            else
                logger.error("Password is NULL");
            return ResponseEntity.badRequest().body("Email and password required");
        }

        String role = req.getRole();
        if (role == null || role.isEmpty()) {
            return ResponseEntity.badRequest().body("Role selection is required for login");
        }

        if ("DONOR".equalsIgnoreCase(role)) {
            Donor d = donorRepository.findByEmail(email);
            if (d != null) {
                if (passwordEncoder.matches(pw, d.getPasswordHash())) {
                    logger.info("Donor logged into system with email={}", email);
                    return ResponseEntity.ok(new AuthResponse(d.getId(), "DONOR", d.getName(), email));
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }
        } else if ("HOSPITAL".equalsIgnoreCase(role)) {
            Hospital h = hospitalRepository.findByEmail(email);
            if (h != null) {
                if (passwordEncoder.matches(pw, h.getPasswordHash())) {
                    logger.info("Hospital logged into system with email={}", email);
                    return ResponseEntity.ok(new AuthResponse(h.getId(), "HOSPITAL", h.getName(), email));
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }
        } else if ("BLOOD_BANK".equalsIgnoreCase(role)) {
            BloodBank bb = bloodBankRepository.findByEmail(email);
            if (bb != null) {
                if (passwordEncoder.matches(pw, bb.getPasswordHash())) {
                    logger.info("Blood Bank logged into system with email={}", email);
                    return ResponseEntity.ok(new AuthResponse(bb.getId(), "BLOOD_BANK", bb.getName(), email));
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }
        }

        // email not found in any collection
        logger.error("User not found for email={}", email);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }
}
