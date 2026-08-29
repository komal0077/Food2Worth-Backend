package com.example.FoodWaste.repository;

import com.example.FoodWaste.entity.Claim;
import com.example.FoodWaste.entity.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Page<Claim> findByVolunteerId(Long volunteerId, Pageable pageable);

    Page<Claim> findByNgoId(Long ngoId, Pageable pageable);

    Page<Claim> findByStatus(ClaimStatus status, Pageable pageable);
}
