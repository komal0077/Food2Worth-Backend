package com.example.FoodWaste.controller;

import com.example.FoodWaste.entity.Claim;
import com.example.FoodWaste.service.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    // Create Claim
    @PostMapping
    public Claim createClaim(@RequestBody Claim claim) {

        return claimService.createClaim(claim);
    }

    // Get All Claims
    @GetMapping
    public List<Claim> getAllClaims() {

        return claimService.getAllClaims();
    }

    // Get Claim By Id
    @GetMapping("/{id}")
    public Claim getClaimById(@PathVariable Long id) {

        return claimService.getClaimById(id);
    }

    // Get Claims By Volunteer
    @GetMapping("/volunteer/{volunteerId}")
    public List<Claim> getClaimsByVolunteer(@PathVariable Long volunteerId) {

        return claimService.getClaimsByVolunteer(volunteerId);
    }

    // Get Claims By NGO
    @GetMapping("/ngo/{ngoId}")
    public List<Claim> getClaimsByNgo(@PathVariable Long ngoId) {

        return claimService.getClaimsByNgo(ngoId);
    }

    // Mark Picked Up
    @PutMapping("/{id}/pickup")
    public Claim markPickedUp(@PathVariable Long id) {

        return claimService.markPickedUp(id);
    }

    // Mark Delivered
    @PutMapping("/{id}/deliver")
    public Claim markDelivered(@PathVariable Long id) {

        return claimService.markDelivered(id);
    }

    // Delete Claim
    @DeleteMapping("/{id}")
    public String deleteClaim(@PathVariable Long id) {

        claimService.deleteClaim(id);

        return "Claim Deleted Successfully";
    }
}
