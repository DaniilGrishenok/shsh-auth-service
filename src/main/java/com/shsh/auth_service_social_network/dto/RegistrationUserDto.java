package com.shsh.auth_service_social_network.dto;

import lombok.Data;

@Data
public class RegistrationUserDto {

    private String email;
    private String username;
    private String password;
    private String confirmPassword;

}