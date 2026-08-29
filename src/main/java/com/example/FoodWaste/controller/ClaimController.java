package com.example.FoodWaste.controller;

import com.example.FoodWaste.dto.ClaimResponse;
import com.example.FoodWaste.entity.Claim;
import com.example.FoodWaste.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    // Create Claim — only NGOs/volunteers can claim a listing
    @PostMapping
    @PreAuthorize("hasAnyAuthority('NGO', 'VOLUNTEER', 'ADMIN')")
    public Claim createClaim(@Valid @RequestBody Claim claim) {

        return claimService.createClaim(claim);
    }

    // Get All Claims
    @GetMapping
    public Page<Claim> getAllClaims(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return claimService.getAllClaims(pageable);
    }

    // Get Claim By Id
    @GetMapping("/{id}")
    public Claim getClaimById(@PathVariable Long id) {

        return claimService.getClaimById(id);
    }

    // Get Claims By Volunteer
    @GetMapping("/volunteer/{volunteerId}")
    public Page<Claim> getClaimsByVolunteer(
            @PathVariable Long volunteerId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return claimService.getClaimsByVolunteer(volunteerId, pageable);
    }

    // Get Claims By NGO
    @GetMapping("/ngo/{ngoId}")
    public Page<Claim> getClaimsByNgo(
            @PathVariable Long ngoId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return claimService.getClaimsByNgo(ngoId, pageable);
    }

    // Mark Picked Up — the assigned volunteer/admin
    @PutMapping("/{id}/pickup")
    @PreAuthorize("hasAnyAuthority('VOLUNTEER', 'ADMIN')")
    public Claim markPickedUp(@PathVariable Long id) {

        return claimService.markPickedUp(id);
    }

    @GetMapping("/details")
    public Page<ClaimResponse> getAllClaimDetails(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return claimService.getAllClaimDetails(pageable);
    }
    // Mark Delivered — the receiving NGO/volunteer/admin
    @PutMapping("/{id}/deliver")
    @PreAuthorize("hasAnyAuthority('NGO', 'VOLUNTEER', 'ADMIN')")
    public Claim markDelivered(@PathVariable Long id) {

        return claimService.markDelivered(id);
    }

    // Delete Claim — admin only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String deleteClaim(@PathVariable Long id) {

        claimService.deleteClaim(id);

        return "Claim Deleted Successfully";
    }
}
