package com.example.FoodWaste.service;

import com.example.FoodWaste.entity.FoodListing;
import com.example.FoodWaste.entity.ListingStatus;
import com.example.FoodWaste.entity.Role;
import com.example.FoodWaste.entity.User;
import com.example.FoodWaste.exception.ResourceNotFoundException;
import com.example.FoodWaste.repository.FoodListingRepository;
import com.example.FoodWaste.repository.UserRepository;
import com.example.FoodWaste.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodListingService {

    private final FoodListingRepository foodListingRepository;

    private final UserRepository userRepository;

    // Create Food Listing
    public FoodListing createListing(FoodListing foodListing) {

        foodListing.setCreatedAt(LocalDateTime.now());

        foodListing.setStatus(ListingStatus.ACTIVE);

        FoodListing saved = foodListingRepository.save(foodListing);

        log.info("Food listing created: id={}, donorId={}", saved.getId(), saved.getDonorId());

        return saved;
    }

    // Get All Listings
    public Page<FoodListing> getAllListings(Pageable pageable) {

        return foodListingRepository.findAll(pageable);
    }

    // Get Listing By Id
    public FoodListing getListingById(Long id) {

        return foodListingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food Listing Not Found"));
    }

    // Get Listings By Status
    public Page<FoodListing> getListingsByStatus(ListingStatus status, Pageable pageable) {

        return foodListingRepository.findByStatus(status, pageable);
    }

    // Get Listings By Donor
    public Page<FoodListing> getListingsByDonor(Long donorId, Pageable pageable) {

        return foodListingRepository.findByDonorId(donorId, pageable);
    }

    // Delete Listing — only the owning donor or an admin may delete
    public void deleteListing(Long id) {

        FoodListing listing = getListingById(id);

        User currentUser = userRepository.findByEmail(SecurityUtils.getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        boolean isOwner = currentUser.getId().equals(listing.getDonorId());

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            log.warn("Unauthorized delete attempt: listingId={}, userId={}", id, currentUser.getId());
            throw new AccessDeniedException("You do not have permission to delete this listing");
        }

        log.info("Food listing deleted: id={}, by userId={}", id, currentUser.getId());

        foodListingRepository.deleteById(id);
    }
}
