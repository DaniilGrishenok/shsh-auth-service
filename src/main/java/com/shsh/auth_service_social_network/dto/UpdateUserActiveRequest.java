package com.shsh.auth_service_social_network.dto;

import lombok.Data;

@Data
public class UpdateUserActiveRequest {
    private String userId;
    private boolean setActiveValue;
}
