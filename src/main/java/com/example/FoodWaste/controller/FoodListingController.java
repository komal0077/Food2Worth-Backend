package com.example.FoodWaste.controller;

import com.example.FoodWaste.entity.FoodListing;
import com.example.FoodWaste.entity.ListingStatus;
import com.example.FoodWaste.service.FoodListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class FoodListingController {

    private final FoodListingService foodListingService;

    // Create Listing — only donors/admins may publish a listing
    @PostMapping
    @PreAuthorize("hasAnyAuthority('DONOR', 'ADMIN')")
    public FoodListing createListing(@Valid @RequestBody FoodListing foodListing) {

        return foodListingService.createListing(foodListing);
    }

    // Get All Listings
    @GetMapping
    public Page<FoodListing> getAllListings(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return foodListingService.getAllListings(pageable);
    }

    // Get Listing By Id
    @GetMapping("/{id}")
    public FoodListing getListingById(@PathVariable Long id) {

        return foodListingService.getListingById(id);
    }

    // Get Listings By Status
    @GetMapping("/status/{status}")
    public Page<FoodListing> getListingsByStatus(
            @PathVariable ListingStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return foodListingService.getListingsByStatus(status, pageable);
    }

    // Get Listings By Donor
    @GetMapping("/donor/{donorId}")
    public Page<FoodListing> getListingsByDonor(
            @PathVariable Long donorId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        return foodListingService.getListingsByDonor(donorId, pageable);
    }

    // Delete Listing — ownership is enforced in the service layer
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity <String> deleteListing(
            @PathVariable Long id) {

        foodListingService.deleteListing(id);

        return ResponseEntity.ok(
                "Food Listing Deleted Successfully");
    }
}
