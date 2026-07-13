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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private FoodListingRepository foodListingRepository;

    @Mock
    private NotificationService notificationService;

    private ClaimService claimService;

    private AuthenticatedUser ngoPrincipal;
    private AuthenticatedUser otherNgoPrincipal;
    private AuthenticatedUser adminPrincipal;

    @BeforeEach
    void setUp() {
        claimService = new ClaimService(claimRepository, foodListingRepository, notificationService);

        ngoPrincipal = new AuthenticatedUser(10L, "Helping Hands NGO", "ngo@example.com", "hash",
                Set.of(new SimpleGrantedAuthority("NGO")));

        otherNgoPrincipal = new AuthenticatedUser(99L, "Rival NGO", "rival@example.com", "hash",
                Set.of(new SimpleGrantedAuthority("NGO")));

        adminPrincipal = new AuthenticatedUser(1L, "Site Admin", "admin@example.com", "hash",
                Set.of(new SimpleGrantedAuthority("ADMIN")));
    }

    @Test
    void createClaimTakesNgoIdentityFromPrincipalNotRequestBody() {

        FoodListing listing = FoodListing.builder()
                .id(5L).donorId(2L).donorName("Donor").title("Rice").status("ACTIVE")
                .build();

        CreateClaimRequest request = new CreateClaimRequest();
        request.setListingId(5L);
        request.setVolunteerName("Vik Volunteer");
        request.setVolunteerPhone("1234567890");

        when(foodListingRepository.findById(5L)).thenReturn(Optional.of(listing));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));

        Claim result = claimService.createClaim(request, ngoPrincipal);

        assertThat(result.getNgoId()).isEqualTo(10L);
        assertThat(result.getNgoName()).isEqualTo("Helping Hands NGO");
        assertThat(result.getStatus()).isEqualTo("CLAIMED");

        verify(foodListingRepository).save(argThat(saved -> "CLAIMED".equals(saved.getStatus())));
        verify(notificationService).createNotification(any(Notification.class));
    }

    @Test
    void createClaimRejectsListingThatIsNotActive() {

        FoodListing listing = FoodListing.builder().id(5L).status("CLAIMED").build();

        CreateClaimRequest request = new CreateClaimRequest();
        request.setListingId(5L);
        request.setVolunteerName("Vik");
        request.setVolunteerPhone("123");

        when(foodListingRepository.findById(5L)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> claimService.createClaim(request, ngoPrincipal))
                .isInstanceOf(IllegalArgumentException.class);

        verify(claimRepository, never()).save(any());
    }

    @Test
    void createClaimRejectsUnknownListing() {

        CreateClaimRequest request = new CreateClaimRequest();
        request.setListingId(404L);
        request.setVolunteerName("Vik");
        request.setVolunteerPhone("123");

        when(foodListingRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> claimService.createClaim(request, ngoPrincipal))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void markPickedUpDeniedForNgoThatDidNotMakeTheClaim() {

        Claim claim = Claim.builder().id(7L).ngoId(10L).status("CLAIMED").build();

        when(claimRepository.findById(7L)).thenReturn(Optional.of(claim));

        assertThatThrownBy(() -> claimService.markPickedUp(7L, otherNgoPrincipal))
                .isInstanceOf(AccessDeniedException.class);

        verify(claimRepository, never()).save(any());
    }

    @Test
    void markPickedUpAllowedForAdminEvenIfNotOwner() {

        Claim claim = Claim.builder().id(7L).listingId(5L).ngoId(10L).status("CLAIMED").build();

        when(claimRepository.findById(7L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));
        when(foodListingRepository.findById(5L)).thenReturn(Optional.of(
                FoodListing.builder().id(5L).donorId(2L).title("Rice").build()));

        Claim result = claimService.markPickedUp(7L, adminPrincipal);

        assertThat(result.getStatus()).isEqualTo("PICKED_UP");
        assertThat(result.getPickedUpAt()).isNotNull();
    }

    @Test
    void markDeliveredCompletesListingAndNotifiesDonorAndNgo() {

        Claim claim = Claim.builder().id(7L).listingId(5L).ngoId(10L).ngoName("Helping Hands NGO").status("PICKED_UP").build();
        FoodListing listing = FoodListing.builder().id(5L).donorId(2L).donorName("Donor").title("Rice").build();

        when(claimRepository.findById(7L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> inv.getArgument(0));
        when(foodListingRepository.findById(5L)).thenReturn(Optional.of(listing));

        Claim result = claimService.markDelivered(7L, ngoPrincipal);

        assertThat(result.getStatus()).isEqualTo("DELIVERED");
        assertThat(result.getDeliveredAt()).isNotNull();
        assertThat(listing.getStatus()).isEqualTo("COMPLETED");
        verify(foodListingRepository).save(listing);
        // one notification to the donor, one to the ngo
        verify(notificationService, times(2)).createNotification(any(Notification.class));
    }

    @Test
    void getMyClaimDetailsBatchesListingLookupInsteadOfOnePerClaim() {

        Claim claim1 = Claim.builder().id(1L).listingId(100L).ngoId(10L).status("CLAIMED").build();
        Claim claim2 = Claim.builder().id(2L).listingId(200L).ngoId(10L).status("CLAIMED").build();

        FoodListing listing1 = FoodListing.builder().id(100L).title("Rice").build();
        FoodListing listing2 = FoodListing.builder().id(200L).title("Bread").build();

        when(claimRepository.findByNgoId(10L)).thenReturn(List.of(claim1, claim2));
        when(foodListingRepository.findAllById(anyList())).thenReturn(List.of(listing1, listing2));

        List<ClaimResponse> responses = claimService.getMyClaimDetails(ngoPrincipal);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(ClaimResponse::getTitle).containsExactlyInAnyOrder("Rice", "Bread");

        // exactly one batched call, never a per-claim findById
        verify(foodListingRepository, times(1)).findAllById(anyList());
        verify(foodListingRepository, never()).findById(any());
    }

    @Test
    void deleteClaimDeniedForNonOwningNonAdmin() {

        Claim claim = Claim.builder().id(7L).ngoId(10L).build();

        when(claimRepository.findById(7L)).thenReturn(Optional.of(claim));

        assertThatThrownBy(() -> claimService.deleteClaim(7L, otherNgoPrincipal))
                .isInstanceOf(AccessDeniedException.class);

        verify(claimRepository, never()).deleteById(any());
    }
}
