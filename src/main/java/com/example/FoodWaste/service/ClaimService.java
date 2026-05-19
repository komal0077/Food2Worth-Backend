package com.example.FoodWaste.service;

import com.example.FoodWaste.entity.Claim;
import com.example.FoodWaste.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;

    // Create Claim
    public Claim createClaim(Claim claim) {

        claim.setCreatedAt(LocalDateTime.now());

        claim.setStatus("CLAIMED");

        return claimRepository.save(claim);
    }

    // Get All Claims
    public List<Claim> getAllClaims() {

        return claimRepository.findAll();
    }

    // Get Claim By Id
    public Claim getClaimById(Long id) {

        return claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim Not Found"));
    }

    // Get Claims By Volunteer
    public List<Claim> getClaimsByVolunteer(Long volunteerId) {

        return claimRepository.findByVolunteerId(volunteerId);
    }

    // Get Claims By NGO
    public List<Claim> getClaimsByNgo(Long ngoId) {

        return claimRepository.findByNgoId(ngoId);
    }

    // Mark Picked Up
    public Claim markPickedUp(Long id) {

        Claim claim = getClaimById(id);

        claim.setStatus("PICKED_UP");

        claim.setPickedUpAt(LocalDateTime.now());

        return claimRepository.save(claim);
    }

    // Mark Delivered
    public Claim markDelivered(Long id) {

        Claim claim = getClaimById(id);

        claim.setStatus("DELIVERED");

        claim.setDeliveredAt(LocalDateTime.now());

        return claimRepository.save(claim);
    }

    // Delete Claim
    public void deleteClaim(Long id) {

        claimRepository.deleteById(id);
    }
}
