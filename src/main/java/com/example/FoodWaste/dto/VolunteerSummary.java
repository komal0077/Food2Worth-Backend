package com.example.FoodWaste.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Minimal, public-safe view of a volunteer for an NGO to pick from when
// assigning a claim — no email/address/verification flags exposed.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolunteerSummary {

    private Long id;

    private String name;

    private String phone;
}
