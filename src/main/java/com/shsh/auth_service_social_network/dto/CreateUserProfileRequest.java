package com.shsh.auth_service_social_network.dto;

import lombok.Data;

@Data
public class CreateUserProfileRequest {

    private String id;
    private String email;
    private String username;

}
