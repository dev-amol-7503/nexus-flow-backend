package com.nexus_flow.nexus_flow.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String bio;
    private Set<Long> roleIds;
    private Boolean active;
}