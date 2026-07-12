package com.example.FoodWaste.service;

import com.example.FoodWaste.dto.ClaimResponse;
import com.example.FoodWaste.entity.Claim;
import com.example.FoodWaste.entity.FoodListing;
import com.example.FoodWaste.entity.Notification;
import com.example.FoodWaste.repository.ClaimRepository;
import com.example.FoodWaste.repository.FoodListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ClaimService {


    private final ClaimRepository claimRepository;

    private final FoodListingRepository foodListingRepository;

    private final NotificationService notificationService;

    // Create Claim
    public Claim createClaim(Claim claim) {

        claim.setCreatedAt(LocalDateTime.now());

        claim.setStatus("CLAIMED");

        Claim savedClaim = claimRepository.save(claim);

        FoodListing listing = foodListingRepository.findById(claim.getListingId()).orElse(null);

        if (listing != null) {

            listing.setStatus("CLAIMED");

            foodListingRepository.save(listing);

            notificationService.createNotification(
                    Notification.builder()
                            .userId(listing.getDonorId())
                            .userName(listing.getDonorName())
                            .type("FOOD_CLAIMED")
                            .message(
                                    (claim.getNgoName() != null ? claim.getNgoName() : "An NGO")
                                            + " claimed your food listing \"" + listing.getTitle() + "\""
                            )
                            .build()
            );
        }

        return savedClaim;
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

        Claim savedClaim = claimRepository.save(claim);

        FoodListing listing = foodListingRepository.findById(claim.getListingId()).orElse(null);

        if (listing != null) {

            notificationService.createNotification(
                    Notification.builder()
                            .userId(listing.getDonorId())
                            .userName(listing.getDonorName())
                            .type("FOOD_PICKED_UP")
                            .message(
                                    (claim.getVolunteerName() != null ? claim.getVolunteerName() : "A volunteer")
                                            + " picked up your food listing \"" + listing.getTitle() + "\""
                            )
                            .build()
            );
        }

        return savedClaim;
    }

    // Mark Delivered
    public Claim markDelivered(Long id) {

        Claim claim = getClaimById(id);

        claim.setStatus("DELIVERED");

        claim.setDeliveredAt(LocalDateTime.now());

        Claim savedClaim = claimRepository.save(claim);

        FoodListing listing = foodListingRepository.findById(claim.getListingId()).orElse(null);

        if (listing != null) {

            listing.setStatus("COMPLETED");

            foodListingRepository.save(listing);

            notificationService.createNotification(
                    Notification.builder()
                            .userId(listing.getDonorId())
                            .userName(listing.getDonorName())
                            .type("FOOD_DELIVERED")
                            .message(
                                    "Your food listing \"" + listing.getTitle() + "\" was delivered successfully"
                            )
                            .build()
            );

            if (claim.getNgoId() != null) {

                notificationService.createNotification(
                        Notification.builder()
                                .userId(claim.getNgoId())
                                .userName(claim.getNgoName())
                                .type("FOOD_DELIVERED")
                                .message(
                                        "Food listing \"" + listing.getTitle() + "\" has been delivered"
                                )
                                .build()
                );
            }
        }

        return savedClaim;
    }

    // Get Claim + Food Listing Details
    // Get Claim + Food Listing Details
    public List<ClaimResponse> getAllClaimDetails() {

        List<Claim> claims = claimRepository.findAll();

        return claims.stream()

                .map(claim -> {

                    // Skip invalid claims
                    if (claim.getListingId() == null) {
                        return null;
                    }

                    FoodListing food =
                            foodListingRepository
                                    .findById(claim.getListingId())
                                    .orElse(null);

                    // Skip if food listing deleted
                    if (food == null) {
                        return null;
                    }

                    return ClaimResponse.builder()

                            .claimId(claim.getId())
                            .status(claim.getStatus())

                            .ngoName(claim.getNgoName())
                            .volunteerName(claim.getVolunteerName())
                            .volunteerPhone(claim.getVolunteerPhone())

                            .listingId(food.getId())
                            .title(food.getTitle())
                            .description(food.getDescription())
                            .photoUrl(food.getPhotoUrl())
                            .category(food.getCategory())
                            .quantity(food.getQuantity())
                            .quantityUnit(food.getQuantityUnit())
                            .address(food.getAddress())

                            .build();

                })

                .filter(Objects::nonNull)

                .toList();
    }

    // Delete Claim
    public void deleteClaim(Long id) {

        claimRepository.deleteById(id);
    }

}
