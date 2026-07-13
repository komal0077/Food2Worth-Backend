package com.example.FoodWaste.service;

import com.example.FoodWaste.dto.CreateListingRequest;
import com.example.FoodWaste.entity.FoodListing;
import com.example.FoodWaste.entity.User;
import com.example.FoodWaste.exception.NotFoundException;
import com.example.FoodWaste.repository.FoodListingRepository;
import com.example.FoodWaste.repository.UserRepository;
import com.example.FoodWaste.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodListingService {

    private static final int MAX_PAGE_SIZE = 200;

    private final FoodListingRepository foodListingRepository;

    private final UserRepository userRepository;

    // Create Food Listing - donor identity always comes from the authenticated user, never the request body
    public FoodListing createListing(CreateListingRequest request, AuthenticatedUser principal) {

        User donor = userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException("User Not Found"));

        FoodListing foodListing = FoodListing.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .quantity(request.getQuantity())
                .quantityUnit(request.getQuantityUnit())
                .photoUrl(request.getPhotoUrl())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .pickupStartTime(request.getPickupStartTime())
                .pickupEndTime(request.getPickupEndTime())
                .expiryTime(request.getExpiryTime())
                .donorId(donor.getId())
                .donorName(donor.getName())
                .donorPhone(donor.getPhone())
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();

        return foodListingRepository.save(foodListing);
    }

    // Get All Listings (capped, most recent first not required - keep insertion order)
    public List<FoodListing> getAllListings(Integer page, Integer size) {

        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? Math.min(size, MAX_PAGE_SIZE) : MAX_PAGE_SIZE;

        return foodListingRepository.findAll(PageRequest.of(pageNumber, pageSize)).getContent();
    }

    // Get Listing By Id
    public FoodListing getListingById(Long id) {

        return foodListingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Food Listing Not Found"));
    }

    // Get Listings By Status
    public List<FoodListing> getListingsByStatus(String status) {

        return foodListingRepository.findByStatus(status);
    }

    // Get Listings By Donor
    public List<FoodListing> getListingsByDonor(Long donorId) {

        return foodListingRepository.findByDonorId(donorId);
    }

    // Delete Listing - only the donor who created it (or an admin) may delete it
    public void deleteListing(Long id, AuthenticatedUser principal) {

        FoodListing listing = getListingById(id);

        if (!principal.isSelfOrAdmin(listing.getDonorId())) {
            throw new AccessDeniedException("You can only delete your own listings");
        }

        foodListingRepository.deleteById(id);
    }
}
