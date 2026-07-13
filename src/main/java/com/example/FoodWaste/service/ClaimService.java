package com.example.FoodWaste.service;

import com.example.FoodWaste.dto.ClaimResponse;
import com.example.FoodWaste.dto.CreateClaimRequest;
import com.example.FoodWaste.entity.Claim;
import com.example.FoodWaste.entity.FoodListing;
import com.example.FoodWaste.entity.Notification;
import com.example.FoodWaste.exception.NotFoundException;
import com.example.FoodWaste.repository.ClaimRepository;
import com.example.FoodWaste.repository.FoodListingRepository;
import com.example.FoodWaste.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private static final int MAX_PAGE_SIZE = 200;

    private final ClaimRepository claimRepository;

    private final FoodListingRepository foodListingRepository;

    private final NotificationService notificationService;

    // Create Claim - ngo identity always comes from the authenticated user, never the request body
    public Claim createClaim(CreateClaimRequest request, AuthenticatedUser principal) {

        FoodListing listing = foodListingRepository.findById(request.getListingId())
                .orElseThrow(() -> new NotFoundException("Food Listing Not Found"));

        if (!"ACTIVE".equals(listing.getStatus())) {
            throw new IllegalArgumentException("This listing is no longer available to claim");
        }

        Claim claim = Claim.builder()
                .listingId(listing.getId())
                .ngoId(principal.getId())
                .ngoName(principal.getFullName())
                .volunteerName(request.getVolunteerName())
                .volunteerPhone(request.getVolunteerPhone())
                .status("CLAIMED")
                .createdAt(LocalDateTime.now())
                .build();

        Claim savedClaim = claimRepository.save(claim);

        listing.setStatus("CLAIMED");
        foodListingRepository.save(listing);

        notificationService.createNotification(
                Notification.builder()
                        .userId(listing.getDonorId())
                        .userName(listing.getDonorName())
                        .type("FOOD_CLAIMED")
                        .message(
                                principal.getFullName() + " claimed your food listing \"" + listing.getTitle() + "\""
                        )
                        .build()
        );

        return savedClaim;
    }

    // Get All Claims (admin only - paginated)
    public List<Claim> getAllClaims(Integer page, Integer size) {

        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? Math.min(size, MAX_PAGE_SIZE) : MAX_PAGE_SIZE;

        return claimRepository.findAll(PageRequest.of(pageNumber, pageSize)).getContent();
    }

    // Get Claim By Id
    public Claim getClaimById(Long id) {

        return claimRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Claim Not Found"));
    }

    // Get Claims By Volunteer
    public List<Claim> getClaimsByVolunteer(Long volunteerId) {

        return claimRepository.findByVolunteerId(volunteerId);
    }

    // Get Claims By NGO
    public List<Claim> getClaimsByNgo(Long ngoId) {

        return claimRepository.findByNgoId(ngoId);
    }

    // Mark Picked Up - only the claiming NGO or an admin
    public Claim markPickedUp(Long id, AuthenticatedUser principal) {

        Claim claim = getClaimById(id);

        requireClaimOwner(claim, principal);

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

    // Mark Delivered - only the claiming NGO or an admin
    public Claim markDelivered(Long id, AuthenticatedUser principal) {

        Claim claim = getClaimById(id);

        requireClaimOwner(claim, principal);

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

    // Get Claim + Food Listing Details (admin only - unscoped, paginated)
    public List<ClaimResponse> getAllClaimDetails(Integer page, Integer size) {

        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? Math.min(size, MAX_PAGE_SIZE) : MAX_PAGE_SIZE;

        List<Claim> claims = claimRepository.findAll(PageRequest.of(pageNumber, pageSize)).getContent();

        return toClaimResponses(claims);
    }

    // Get Claim + Food Listing Details scoped to the authenticated NGO's own claims
    public List<ClaimResponse> getMyClaimDetails(AuthenticatedUser principal) {

        List<Claim> claims = claimRepository.findByNgoId(principal.getId());

        return toClaimResponses(claims);
    }

    // Batches the food listing lookups instead of one query per claim
    private List<ClaimResponse> toClaimResponses(List<Claim> claims) {

        List<Long> listingIds = claims.stream()
                .map(Claim::getListingId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, FoodListing> listingsById = foodListingRepository.findAllById(listingIds).stream()
                .collect(Collectors.toMap(FoodListing::getId, Function.identity()));

        return claims.stream()
                .map(claim -> {

                    FoodListing food = claim.getListingId() != null
                            ? listingsById.get(claim.getListingId())
                            : null;

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
                            .donorId(food.getDonorId())
                            .donorName(food.getDonorName())
                            .title(food.getTitle())
                            .description(food.getDescription())
                            .photoUrl(food.getPhotoUrl())
                            .category(food.getCategory())
                            .quantity(food.getQuantity())
                            .quantityUnit(food.getQuantityUnit())
                            .address(food.getAddress())

                            .build();

                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    // Delete Claim - only the claiming NGO or an admin
    public void deleteClaim(Long id, AuthenticatedUser principal) {

        Claim claim = getClaimById(id);

        requireClaimOwner(claim, principal);

        claimRepository.deleteById(id);
    }

    private void requireClaimOwner(Claim claim, AuthenticatedUser principal) {

        if (!principal.isSelfOrAdmin(claim.getNgoId())) {
            throw new AccessDeniedException("You can only manage claims you made");
        }
    }
}
