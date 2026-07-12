package com.example.FoodWaste.controller;

import com.example.FoodWaste.entity.FoodListing;
import com.example.FoodWaste.service.FoodListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class FoodListingController {

    private final FoodListingService foodListingService;

    // Create Listing
    @PostMapping
    public FoodListing createListing(@RequestBody FoodListing foodListing) {

        return foodListingService.createListing(foodListing);
    }

    // Get All Listings
    @GetMapping
    public List<FoodListing> getAllListings() {

        return foodListingService.getAllListings();
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

    // Delete Listing
    @DeleteMapping("/{id}")
    public ResponseEntity <String> deleteListing(
            @PathVariable Long id) {

        foodListingService.deleteListing(id);

        return ResponseEntity.ok(
                "Food Listing Deleted Successfully");
    }
}
