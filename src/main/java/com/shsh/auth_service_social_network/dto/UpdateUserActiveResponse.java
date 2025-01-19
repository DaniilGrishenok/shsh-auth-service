package com.shsh.auth_service_social_network.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserActiveResponse {
    private String message;
    private String userId;
    private String isActive;
}