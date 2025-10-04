package com.nexus_flow.nexus_flow.dto;

import com.nexus_flow.nexus_flow.entity.Role;
import lombok.Data;

import java.util.List; // Change from Set to List

@Data
public class JwtResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private List<Role> roles; // Change from Set to List

    public JwtResponse(String accessToken, Long id, String username, String email,
                       String firstName, String lastName, List<Role> roles) { // Change parameter type
        this.accessToken = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
    }
}