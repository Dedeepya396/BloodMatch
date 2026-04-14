package com.example.bloodmatch.service;

import com.example.bloodmatch.dto.DonorResponse;
import com.example.bloodmatch.model.BloodRequest;
import com.example.bloodmatch.model.Donor;
import com.example.bloodmatch.observer.DonorNotifier;
import com.example.bloodmatch.repository.DonorRepository;
import com.example.bloodmatch.strategy.CompatibilityFirstStrategy;
import com.example.bloodmatch.strategy.MatchingStrategy;
import com.example.bloodmatch.strategy.SmartMatchingStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    private final DonorRepository donorRepository;
    private final DonorNotifier donorNotifier;

    public MatchingService(DonorRepository donorRepository, DonorNotifier donorNotifier) {
        this.donorRepository = donorRepository;
        this.donorNotifier = donorNotifier;
    }

    public List<DonorResponse> findMatches(BloodRequest request) {
        MatchingStrategy strategy = "HIGH".equalsIgnoreCase(request.getUrgency())
                ? new SmartMatchingStrategy()
                : new CompatibilityFirstStrategy();

        List<Donor> donors = donorRepository.findAll();
        List<DonorResponse> matched = strategy.match(donors, request);

        // Notifier still works with Donor objects
        donorNotifier.notify(
                matched.stream().map(DonorResponse::getDonor).collect(Collectors.toList()),
                request);

        return matched;
    }
}