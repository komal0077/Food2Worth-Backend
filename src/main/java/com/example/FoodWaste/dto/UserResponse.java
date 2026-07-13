package com.example.FoodWaste.dto;

import com.example.FoodWaste.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String address;
    private Double latitude;
    private Double longitude;
    private String profilePhoto;
    private Boolean isVerified;
    private Boolean isApproved;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .address(user.getAddress())
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .profilePhoto(user.getProfilePhoto())
                .isVerified(user.getIsVerified())
                .isApproved(user.getIsApproved())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
