package com.shsh.auth_service_social_network.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class JwtRequest {
    private String username;
    private String password;
}
