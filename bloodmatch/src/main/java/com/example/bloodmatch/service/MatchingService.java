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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MatchingService {

    private final DonorRepository donorRepository;
    private final DonorNotifier donorNotifier;
    private static final Logger logger = LoggerFactory.getLogger(MatchingService.class);

    public MatchingService(DonorRepository donorRepository, DonorNotifier donorNotifier) {
        this.donorRepository = donorRepository;
        this.donorNotifier = donorNotifier;
    }

    public List<DonorResponse> findMatches(BloodRequest request) {
        logger.info("Starting donor matching for requestId={}, bloodGroup={}, urgency={}",
                request.getId(), request.getBloodGroupRequired(), request.getUrgency());

        MatchingStrategy strategy = "HIGH".equalsIgnoreCase(request.getUrgency())
                ? new SmartMatchingStrategy()
                : new CompatibilityFirstStrategy();
        logger.debug("Selected matching strategy: {}", strategy.getClass().getSimpleName());

        List<Donor> donors = donorRepository.findAll();
        logger.debug("Fetched {} donors from database", donors.size());
        List<DonorResponse> matched = strategy.match(donors, request);
        logger.info("Matching completed for requestId={}, matchedDonors={}",
                request.getId(), matched.size());

        // Notifier still works with Donor objects
        try {
            List<Donor> donorList = matched.stream()
                    .map(DonorResponse::getDonor)
                    .collect(Collectors.toList());

            logger.info("Sending notifications to {} donors for requestId={}",
                    donorList.size(), request.getId());

            donorNotifier.notify(donorList, request);

            logger.info("Notifications sent successfully for requestId={}", request.getId());

        } catch (Exception ex) {
            logger.error("Failed to send notifications for requestId={}", request.getId(), ex);
        }

        return matched;
    }
}