package com.example.FoodWaste.service;

import com.example.FoodWaste.entity.Claim;
import com.example.FoodWaste.entity.ClaimStatus;
import com.example.FoodWaste.entity.FoodListing;
import com.example.FoodWaste.entity.ListingStatus;
import com.example.FoodWaste.entity.Notification;
import com.example.FoodWaste.entity.Role;
import com.example.FoodWaste.entity.User;
import com.example.FoodWaste.exception.ResourceNotFoundException;
import com.example.FoodWaste.repository.ClaimRepository;
import com.example.FoodWaste.repository.FoodListingRepository;
import com.example.FoodWaste.repository.UserRepository;
import com.example.FoodWaste.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    private static final String CURRENT_USER_EMAIL = "ngo@example.com";

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private FoodListingRepository foodListingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ClaimService claimService;

    private MockedStatic<SecurityUtils> securityUtils;

    @BeforeEach
    void mockCurrentUser() {

        securityUtils = Mockito.mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getCurrentUserEmail).thenReturn(CURRENT_USER_EMAIL);

        User currentUser = User.builder()
                .id(99L)
                .email(CURRENT_USER_EMAIL)
                .name("Helping Hands")
                .role(Role.NGO)
                .build();

        Mockito.lenient().when(userRepository.findByEmail(CURRENT_USER_EMAIL)).thenReturn(Optional.of(currentUser));
    }

    @AfterEach
    void closeStaticMock() {

        securityUtils.close();
    }

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

        // Client attempts to claim on behalf of a different NGO — this must
        // be ignored in favor of the authenticated user's own identity.
        Claim claim = Claim.builder().listingId(10L).ngoId(1234L).ngoName("Someone Else").build();

        when(foodListingRepository.findById(10L)).thenReturn(Optional.of(listing));
        when(foodListingRepository.saveAndFlush(any(FoodListing.class))).thenReturn(listing);
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));

        Claim result = claimService.createClaim(claim);

        assertThat(result.getStatus()).isEqualTo(ClaimStatus.CLAIMED);
        assertThat(result.getNgoId()).isEqualTo(99L);
        assertThat(result.getNgoName()).isEqualTo("Helping Hands");
        assertThat(listing.getStatus()).isEqualTo(ListingStatus.CLAIMED);
        verify(notificationService).createNotification(any(Notification.class));
    }

    @Test
    void createClaim_ngoAssignsRealVolunteerByIdIgnoringClientSuppliedText() {

        FoodListing listing = FoodListing.builder()
                .id(10L)
                .title("Bread")
                .donorId(5L)
                .donorName("Donor Dan")
                .status(ListingStatus.ACTIVE)
                .build();

        User volunteer = User.builder()
                .id(55L)
                .name("Real Volunteer")
                .phone("555-0000")
                .role(Role.VOLUNTEER)
                .build();

        when(userRepository.findById(55L)).thenReturn(Optional.of(volunteer));

        // NGO picks volunteer 55 from a dropdown but also (incorrectly, or
        // by an old client) sends stale free-text name/phone — the real
        // account's own record must win.
        Claim claim = Claim.builder()
                .listingId(10L)
                .volunteerId(55L)
                .volunteerName("Stale Text Name")
                .volunteerPhone("000-0000")
                .build();

        when(foodListingRepository.findById(10L)).thenReturn(Optional.of(listing));
        when(foodListingRepository.saveAndFlush(any(FoodListing.class))).thenReturn(listing);
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));

        Claim result = claimService.createClaim(claim);

        assertThat(result.getVolunteerId()).isEqualTo(55L);
        assertThat(result.getVolunteerName()).isEqualTo("Real Volunteer");
        assertThat(result.getVolunteerPhone()).isEqualTo("555-0000");
    }

    @Test
    void createClaim_rejectsVolunteerIdThatIsNotARealVolunteerAccount() {

        FoodListing listing = FoodListing.builder()
                .id(10L)
                .title("Bread")
                .status(ListingStatus.ACTIVE)
                .build();

        when(userRepository.findById(55L)).thenReturn(Optional.empty());
        when(foodListingRepository.findById(10L)).thenReturn(Optional.of(listing));

        Claim claim = Claim.builder().listingId(10L).volunteerId(55L).build();

        assertThatThrownBy(() -> claimService.createClaim(claim))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Selected volunteer not found");

        verify(claimRepository, never()).save(any());
    }

    @Test
    void createClaim_derivesVolunteerIdentityFromAuthenticatedUser() {

        User volunteer = User.builder()
                .id(77L)
                .email(CURRENT_USER_EMAIL)
                .name("Val Volunteer")
                .phone("555-1234")
                .role(Role.VOLUNTEER)
                .build();

        when(userRepository.findByEmail(CURRENT_USER_EMAIL)).thenReturn(Optional.of(volunteer));

        FoodListing listing = FoodListing.builder()
                .id(10L)
                .title("Bread")
                .donorId(5L)
                .status(ListingStatus.ACTIVE)
                .build();

        Claim claim = Claim.builder().listingId(10L).volunteerId(1234L).build();

        when(foodListingRepository.findById(10L)).thenReturn(Optional.of(listing));
        when(foodListingRepository.saveAndFlush(any(FoodListing.class))).thenReturn(listing);
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));

        Claim result = claimService.createClaim(claim);

        assertThat(result.getVolunteerId()).isEqualTo(77L);
        assertThat(result.getVolunteerName()).isEqualTo("Val Volunteer");
        assertThat(result.getVolunteerPhone()).isEqualTo("555-1234");
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
    void markPickedUp_surfacesConcurrentStatusChangeAsConflict() {

        Claim claim = Claim.builder().id(1L).listingId(10L).status(ClaimStatus.CLAIMED).build();

        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.saveAndFlush(any(Claim.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Claim.class, 1L));

        assertThatThrownBy(() -> claimService.markPickedUp(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("updated by someone else");
    }

    @Test
    void markPickedUp_happyPathNotifiesDonor() {

        Claim claim = Claim.builder()
                .id(1L)
                .listingId(10L)
                .volunteerName("Val Volunteer")
                .status(ClaimStatus.CLAIMED)
                .build();

        FoodListing listing = FoodListing.builder()
                .id(10L)
                .title("Bread")
                .donorId(5L)
                .status(ListingStatus.CLAIMED)
                .build();

        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.saveAndFlush(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));
        when(foodListingRepository.findById(10L)).thenReturn(Optional.of(listing));

        Claim result = claimService.markPickedUp(1L);

        assertThat(result.getStatus()).isEqualTo(ClaimStatus.PICKED_UP);
        verify(notificationService).createNotification(any(Notification.class));
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
        when(claimRepository.saveAndFlush(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));
        when(foodListingRepository.findById(10L)).thenReturn(Optional.of(listing));

        Claim result = claimService.markDelivered(1L);

        assertThat(result.getStatus()).isEqualTo(ClaimStatus.DELIVERED);
        assertThat(listing.getStatus()).isEqualTo(ListingStatus.COMPLETED);
        // one notification for the donor, one for the NGO
        verify(notificationService, org.mockito.Mockito.times(2))
                .createNotification(any(Notification.class));
    }

    @Test
    void getAllClaimDetails_scopesToTheCallingNgosOwnClaims() {

        Claim claim = Claim.builder().id(1L).listingId(10L).status(ClaimStatus.CLAIMED).build();

        FoodListing listing = FoodListing.builder().id(10L).title("Bread").build();

        when(claimRepository.findByNgoId(any(Long.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(claim)));
        when(foodListingRepository.findAllById(any())).thenReturn(List.of(listing));

        var result = claimService.getAllClaimDetails(Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        verify(claimRepository).findByNgoId(99L, Pageable.unpaged());
        verify(claimRepository, never()).findByVolunteerId(any(), any());
    }

    @Test
    void getAllClaimDetails_scopesToTheCallingVolunteersOwnClaims() {

        User volunteer = User.builder()
                .id(77L)
                .email(CURRENT_USER_EMAIL)
                .name("Val Volunteer")
                .role(Role.VOLUNTEER)
                .build();

        when(userRepository.findByEmail(CURRENT_USER_EMAIL)).thenReturn(Optional.of(volunteer));

        Claim claim = Claim.builder().id(1L).listingId(10L).status(ClaimStatus.CLAIMED).build();

        FoodListing listing = FoodListing.builder().id(10L).title("Bread").build();

        when(claimRepository.findByVolunteerId(any(Long.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(claim)));
        when(foodListingRepository.findAllById(any())).thenReturn(List.of(listing));

        var result = claimService.getAllClaimDetails(Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        verify(claimRepository).findByVolunteerId(77L, Pageable.unpaged());
        verify(claimRepository, never()).findByNgoId(any(), any());
    }
}
