package com.example.FoodWaste.repository;

import com.example.FoodWaste.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByVolunteerId(Long volunteerId);

    List<Claim> findByNgoId(Long ngoId);

    List<Claim> findByStatus(String status);
}
