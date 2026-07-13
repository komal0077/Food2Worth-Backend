package com.example.FoodWaste.controller;

import com.example.FoodWaste.dto.CreateListingRequest;
import com.example.FoodWaste.entity.FoodListing;
import com.example.FoodWaste.security.AuthenticatedUser;
import com.example.FoodWaste.service.FoodListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class FoodListingController {

    private final FoodListingService foodListingService;

    // Create Listing - requires auth, donor identity taken from the logged-in user
    @PostMapping
    public FoodListing createListing(
            @Valid @RequestBody CreateListingRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        return foodListingService.createListing(request, principal);
    }

    // Get All Listings (public, paginated)
    @GetMapping
    public List<FoodListing> getAllListings(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {

        return foodListingService.getAllListings(page, size);
    }

    // Get Listing By Id
    @GetMapping("/{id}")
    public FoodListing getListingById(@PathVariable Long id) {

        return foodListingService.getListingById(id);
    }

    // Get Listings By Status
    @GetMapping("/status/{status}")
    public List<FoodListing> getListingsByStatus(@PathVariable String status) {

        return foodListingService.getListingsByStatus(status);
    }

    // Get Listings By Donor
    @GetMapping("/donor/{donorId}")
    public List<FoodListing> getListingsByDonor(@PathVariable Long donorId) {

        return foodListingService.getListingsByDonor(donorId);
    }

    // Delete Listing - only the owning donor or an admin
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteListing(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        foodListingService.deleteListing(id, principal);

        return ResponseEntity.ok(
                "Food Listing Deleted Successfully");
    }
}
