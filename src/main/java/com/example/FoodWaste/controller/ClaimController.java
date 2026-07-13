package com.example.FoodWaste.controller;

import com.example.FoodWaste.dto.ClaimResponse;
import com.example.FoodWaste.dto.CreateClaimRequest;
import com.example.FoodWaste.entity.Claim;
import com.example.FoodWaste.security.AuthenticatedUser;
import com.example.FoodWaste.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    // Create Claim - requires auth, ngo identity taken from the logged-in user
    @PostMapping
    public Claim createClaim(
            @Valid @RequestBody CreateClaimRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        return claimService.createClaim(request, principal);
    }

    // Get All Claims - admin only, exposes every claim across every NGO
    @GetMapping
    public List<Claim> getAllClaims(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        requireAdmin(principal);

        return claimService.getAllClaims(page, size);
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

    // Mark Picked Up - only the claiming NGO or an admin
    @PutMapping("/{id}/pickup")
    public Claim markPickedUp(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        return claimService.markPickedUp(id, principal);
    }

    // Claim + food listing details for every claim - admin only
    @GetMapping("/details")
    public List<ClaimResponse> getAllClaimDetails(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        requireAdmin(principal);

        return claimService.getAllClaimDetails(page, size);
    }

    // Claim + food listing details scoped to the logged-in NGO's own claims
    @GetMapping("/details/mine")
    public List<ClaimResponse> getMyClaimDetails(@AuthenticationPrincipal AuthenticatedUser principal) {

        return claimService.getMyClaimDetails(principal);
    }

    // Mark Delivered - only the claiming NGO or an admin
    @PutMapping("/{id}/deliver")
    public Claim markDelivered(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        return claimService.markDelivered(id, principal);
    }

    // Delete Claim - only the claiming NGO or an admin
    @DeleteMapping("/{id}")
    public String deleteClaim(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        claimService.deleteClaim(id, principal);

        return "Claim Deleted Successfully";
    }

    private void requireAdmin(AuthenticatedUser principal) {
        if (!principal.isAdmin()) {
            throw new AccessDeniedException("Admin access required");
        }
    }
}
