package com.hqc.hophuddles.dto.request;

import com.hqc.hophuddles.enums.Discipline;
import com.hqc.hophuddles.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Agency ID is required")
    private Long agencyId;

    @NotNull(message = "Role is required")
    private UserRole role;

    @NotNull(message = "Discipline is required")
    private Discipline discipline;

    // Optional fields
    private String phone;
    private Long branchId;
    private Long teamId;
    private String profilePictureUrl;
}