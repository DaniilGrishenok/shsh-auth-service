package com.shsh.auth_service_social_network.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String username;
    private String email;

}