package com.shsh.auth_service_social_network.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationEvent {
    private UUID userId;
    private String username;
    private String email;
}