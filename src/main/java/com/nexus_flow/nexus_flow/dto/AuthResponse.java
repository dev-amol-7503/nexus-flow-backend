package com.nexus_flow.nexus_flow.dto;

import com.nexus_flow.nexus_flow.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    public static AuthResponse from(User user) {
        return new AuthResponse(
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}