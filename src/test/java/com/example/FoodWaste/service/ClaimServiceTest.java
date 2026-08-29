package com.example.FoodWaste.service;

import com.example.FoodWaste.entity.Claim;
import com.example.FoodWaste.entity.ClaimStatus;
import com.example.FoodWaste.entity.FoodListing;
import com.example.FoodWaste.entity.ListingStatus;
import com.example.FoodWaste.entity.Notification;
import com.example.FoodWaste.exception.ResourceNotFoundException;
import com.example.FoodWaste.repository.ClaimRepository;
import com.example.FoodWaste.repository.FoodListingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private FoodListingRepository foodListingRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ClaimService claimService;

    @Test
    void createClaim_rejectsListingThatIsNotActive() {

        FoodListing listing = FoodListing.builder()
                .id(10L)
                .title("Bread")
                .status(ListingStatus.CLAIMED)
                .build();

        Claim claim = Claim.builder().listingId(10L).build();

        when(foodListingRepository.findById(10L)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> claimService.createClaim(claim))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no longer available");

        verify(claimRepository, never()).save(any());
    }

    @Test
    void createClaim_rejectsMissingListing() {

        Claim claim = Claim.builder().listingId(999L).build();

        when(foodListingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> claimService.createClaim(claim))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(claimRepository, never()).save(any());
    }

    @Test
    void createClaim_surfacesConcurrentClaimAsConflict() {

        FoodListing listing = FoodListing.builder()
                .id(10L)
                .title("Bread")
                .status(ListingStatus.ACTIVE)
                .build();

        Claim claim = Claim.builder().listingId(10L).build();

        when(foodListingRepository.findById(10L)).thenReturn(Optional.of(listing));
        when(foodListingRepository.saveAndFlush(any(FoodListing.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(FoodListing.class, 10L));

        assertThatThrownBy(() -> claimService.createClaim(claim))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("claimed by someone else");

        verify(claimRepository, never()).save(any());
    }

    @Test
    void createClaim_happyPathFlipsListingStatusAndNotifiesDonor() {

        FoodListing listing = FoodListing.builder()
                .id(10L)
                .title("Bread")
                .donorId(5L)
                .donorName("Donor Dan")
                .status(ListingStatus.ACTIVE)
                .build();

        Claim claim = Claim.builder().listingId(10L).ngoName("Helping Hands").build();

        when(foodListingRepository.findById(10L)).thenReturn(Optional.of(listing));
        when(foodListingRepository.saveAndFlush(any(FoodListing.class))).thenReturn(listing);
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));

        Claim result = claimService.createClaim(claim);

        assertThat(result.getStatus()).isEqualTo(ClaimStatus.CLAIMED);
        assertThat(listing.getStatus()).isEqualTo(ListingStatus.CLAIMED);
        verify(notificationService).createNotification(any(Notification.class));
    }

    @Test
    void markPickedUp_rejectsClaimNotInClaimedState() {

        Claim claim = Claim.builder().id(1L).listingId(10L).status(ClaimStatus.DELIVERED).build();

        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        assertThatThrownBy(() -> claimService.markPickedUp(1L))
                .isInstanceOf(IllegalStateException.class);

        verify(claimRepository, never()).save(any());
    }

    @Test
    void markDelivered_rejectsClaimNotInPickedUpState() {

        Claim claim = Claim.builder().id(1L).listingId(10L).status(ClaimStatus.CLAIMED).build();

        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        assertThatThrownBy(() -> claimService.markDelivered(1L))
                .isInstanceOf(IllegalStateException.class);

        verify(claimRepository, never()).save(any());
    }

    @Test
    void markDelivered_completesListingAndNotifiesBothParties() {

        Claim claim = Claim.builder()
                .id(1L)
                .listingId(10L)
                .ngoId(20L)
                .ngoName("Helping Hands")
                .status(ClaimStatus.PICKED_UP)
                .build();

        FoodListing listing = FoodListing.builder()
                .id(10L)
                .title("Bread")
                .donorId(5L)
                .status(ListingStatus.CLAIMED)
                .build();

        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));
        when(foodListingRepository.findById(10L)).thenReturn(Optional.of(listing));

        Claim result = claimService.markDelivered(1L);

        assertThat(result.getStatus()).isEqualTo(ClaimStatus.DELIVERED);
        assertThat(listing.getStatus()).isEqualTo(ListingStatus.COMPLETED);
        // one notification for the donor, one for the NGO
        verify(notificationService, org.mockito.Mockito.times(2))
                .createNotification(any(Notification.class));
    }
}
