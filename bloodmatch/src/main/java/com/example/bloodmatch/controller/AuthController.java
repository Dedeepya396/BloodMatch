package com.example.bloodmatch.controller;

import com.example.bloodmatch.model.Donor;
import com.example.bloodmatch.model.Hospital;
import com.example.bloodmatch.model.factory.UserFactory;
import com.example.bloodmatch.repository.DonorRepository;
import com.example.bloodmatch.repository.HospitalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.bloodmatch.dto.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final DonorRepository donorRepository;
    private final HospitalRepository hospitalRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(DonorRepository donorRepository, HospitalRepository hospitalRepository) {
        this.donorRepository = donorRepository;
        this.hospitalRepository = hospitalRepository;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        String role = req.getRole();
        String email = req.getEmail();
        if (email == null || req.getPassword() == null) {
            return ResponseEntity.badRequest().body("Email and password required");
        }
        // if user is donor
        if ("DONOR".equalsIgnoreCase(role)) {
            // if email already exists
            if (donorRepository.findByEmail(email) != null) {
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
            return ResponseEntity.ok(new AuthResponse(saved.getId(), "DONOR", saved.getName(), email));

        }
        // if user is a hospital
        else if ("HOSPITAL".equalsIgnoreCase(role)) {
            if (hospitalRepository.findByEmail(email) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered as hospital");
            }
            String hash = passwordEncoder.encode(req.getPassword());
            // directly create a hospital
            Hospital hospital = UserFactory.createHospital(req.getName(), email, hash, req.getAddress(),
                    req.getLatitude(), req.getLongitude(), req.getContactNumber());
            Hospital saved = hospitalRepository.save(hospital);
            return ResponseEntity.ok(new AuthResponse(saved.getId(), "HOSPITAL", saved.getName(), email));
        }
        // new role
        return ResponseEntity.badRequest().body("Unknown role");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        String email = req.getEmail();
        String pw = req.getPassword();
        if (email == null || pw == null)
            return ResponseEntity.badRequest().body("Email and password required");

        Donor d = donorRepository.findByEmail(email);
        // check if this mail is registered as donor
        if (d != null) {
            if (passwordEncoder.matches(pw, d.getPasswordHash())) {
                // login
                return ResponseEntity.ok(new AuthResponse(d.getId(), "DONOR", d.getName(), email));
            }
            System.out.println("AuthController.login()");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        Hospital h = hospitalRepository.findByEmail(email);
        // check if mail is registered as hospital
        if (h != null) {
            if (passwordEncoder.matches(pw, h.getPasswordHash())) {
                // login
                return ResponseEntity.ok(new AuthResponse(h.getId(), "HOSPITAL", h.getName(), email));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        // email is present
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }
}
