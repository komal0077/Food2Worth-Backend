package com.example.FoodWaste.service;

import com.example.FoodWaste.dto.ClaimResponse;
import com.example.FoodWaste.entity.Claim;
import com.example.FoodWaste.entity.ClaimStatus;
import com.example.FoodWaste.entity.FoodListing;
import com.example.FoodWaste.entity.ListingStatus;
import com.example.FoodWaste.entity.Notification;
import com.example.FoodWaste.entity.NotificationType;
import com.example.FoodWaste.exception.ResourceNotFoundException;
import com.example.FoodWaste.repository.ClaimRepository;
import com.example.FoodWaste.repository.FoodListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;

    private final FoodListingRepository foodListingRepository;

    private final NotificationService notificationService;

    // Create Claim — atomic with the listing status flip, and guarded
    // against two callers claiming the same listing at the same time.
    @Transactional
    public Claim createClaim(Claim claim) {

        FoodListing listing = foodListingRepository.findById(claim.getListingId())
                .orElseThrow(() -> new ResourceNotFoundException("Food Listing Not Found"));

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalStateException("This listing is no longer available to claim");
        }

        claim.setCreatedAt(LocalDateTime.now());

        claim.setStatus(ClaimStatus.CLAIMED);

        Claim savedClaim;

        listing.setStatus(ListingStatus.CLAIMED);

        try {
            // @Version on FoodListing makes this throw if another
            // transaction claimed the same listing concurrently.
            foodListingRepository.saveAndFlush(listing);

            savedClaim = claimRepository.save(claim);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Concurrent claim conflict on listingId={}", listing.getId());
            throw new IllegalStateException("This listing was just claimed by someone else");
        }

        log.info("Claim created: claimId={}, listingId={}", savedClaim.getId(), listing.getId());

        notificationService.createNotification(
                Notification.builder()
                        .userId(listing.getDonorId())
                        .userName(listing.getDonorName())
                        .type(NotificationType.FOOD_CLAIMED)
                        .message(
                                (claim.getNgoName() != null ? claim.getNgoName() : "An NGO")
                                        + " claimed your food listing \"" + listing.getTitle() + "\""
                        )
                        .build()
        );

        return savedClaim;
    }

    // Get All Claims
    public Page<Claim> getAllClaims(Pageable pageable) {

        return claimRepository.findAll(pageable);
    }

    // Get Claim By Id
    public Claim getClaimById(Long id) {

        return claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim Not Found"));
    }

    // Get Claims By Volunteer
    public Page<Claim> getClaimsByVolunteer(Long volunteerId, Pageable pageable) {

        return claimRepository.findByVolunteerId(volunteerId, pageable);
    }

    // Get Claims By NGO
    public Page<Claim> getClaimsByNgo(Long ngoId, Pageable pageable) {

        return claimRepository.findByNgoId(ngoId, pageable);
    }

    // Mark Picked Up
    @Transactional
    public Claim markPickedUp(Long id) {

        Claim claim = getClaimById(id);

        if (claim.getStatus() != ClaimStatus.CLAIMED) {
            throw new IllegalStateException("Claim must be CLAIMED before it can be picked up");
        }

        claim.setStatus(ClaimStatus.PICKED_UP);

        claim.setPickedUpAt(LocalDateTime.now());

        Claim savedClaim = claimRepository.save(claim);

        FoodListing listing = foodListingRepository.findById(claim.getListingId())
                .orElseThrow(() -> new ResourceNotFoundException("Food Listing Not Found"));

        log.info("Claim picked up: claimId={}", claim.getId());

        notificationService.createNotification(
                Notification.builder()
                        .userId(listing.getDonorId())
                        .userName(listing.getDonorName())
                        .type(NotificationType.FOOD_PICKED_UP)
                        .message(
                                (claim.getVolunteerName() != null ? claim.getVolunteerName() : "A volunteer")
                                        + " picked up your food listing \"" + listing.getTitle() + "\""
                        )
                        .build()
        );

        return savedClaim;
    }

    // Mark Delivered
    @Transactional
    public Claim markDelivered(Long id) {

        Claim claim = getClaimById(id);

        if (claim.getStatus() != ClaimStatus.PICKED_UP) {
            throw new IllegalStateException("Claim must be PICKED_UP before it can be delivered");
        }

        claim.setStatus(ClaimStatus.DELIVERED);

        claim.setDeliveredAt(LocalDateTime.now());

        Claim savedClaim = claimRepository.save(claim);

        FoodListing listing = foodListingRepository.findById(claim.getListingId())
                .orElseThrow(() -> new ResourceNotFoundException("Food Listing Not Found"));

        listing.setStatus(ListingStatus.COMPLETED);

        foodListingRepository.save(listing);

        log.info("Claim delivered: claimId={}, listingId={}", claim.getId(), listing.getId());

        notificationService.createNotification(
                Notification.builder()
                        .userId(listing.getDonorId())
                        .userName(listing.getDonorName())
                        .type(NotificationType.FOOD_DELIVERED)
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
                            .type(NotificationType.FOOD_DELIVERED)
                            .message(
                                    "Food listing \"" + listing.getTitle() + "\" has been delivered"
                            )
                            .build()
            );
        }

        return savedClaim;
    }

    // Get Claim + Food Listing Details
    public Page<ClaimResponse> getAllClaimDetails(Pageable pageable) {

        Page<Claim> claims = claimRepository.findAll(pageable);

        List<Long> listingIds = claims.stream()
                .map(Claim::getListingId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // One query for all listings on this page instead of one query per claim
        Map<Long, FoodListing> listingsById = foodListingRepository
                .findAllById(listingIds).stream()
                .collect(Collectors.toMap(FoodListing::getId, Function.identity()));

        return claims.map(claim -> {

            // Skip invalid claims
            if (claim.getListingId() == null) {
                return null;
            }

            FoodListing food = listingsById.get(claim.getListingId());

            // Skip if food listing deleted
            if (food == null) {
                return null;
            }

            return ClaimResponse.builder()

                    .claimId(claim.getId())
                    .status(claim.getStatus().name())

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
        });
    }

    // Delete Claim
    public void deleteClaim(Long id) {

        log.info("Deleting claim: claimId={}", id);

        claimRepository.deleteById(id);
    }

}
