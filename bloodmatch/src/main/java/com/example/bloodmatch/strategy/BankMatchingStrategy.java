package com.example.bloodmatch.strategy;

import com.example.bloodmatch.dto.MatchResponse;
import com.example.bloodmatch.model.BloodBank;
import com.example.bloodmatch.model.BloodRequest;
import com.example.bloodmatch.service.BloodBankService;

import java.util.List;

public interface BankMatchingStrategy {
    List<MatchResponse> matchBanks(List<BloodBank> allBanks, BloodRequest request,
                                   boolean applyDistanceFilter, BloodBankService bloodBankService);
}
