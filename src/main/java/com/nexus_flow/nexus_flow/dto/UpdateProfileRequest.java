package com.nexus_flow.nexus_flow.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String firstName;

    private String lastName;

    @Email(message = "Email should be valid")
    private String email;

    private String phone;

    private String bio;
}