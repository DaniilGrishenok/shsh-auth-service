package com.shsh.auth_service_social_network.service;

import com.shsh.auth_service_social_network.dto.JwtResponse;
import com.shsh.auth_service_social_network.dto.UserRegistrationEvent;
import com.shsh.auth_service_social_network.model.User;
import com.shsh.auth_service_social_network.repository.UserRepository;
import com.shsh.auth_service_social_network.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    public ResponseEntity<?> refreshJwtToken(String refreshToken) {
        if (!jwtUtils.validateJwtToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }
        String email = jwtUtils.getEmailFromJwtToken(refreshToken);
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        String newJwtToken = jwtUtils.generateJwtToken(
                user.getEmail(),
                String.valueOf(user.getId())
        );
        JwtResponse jwtResponse = new JwtResponse(newJwtToken, refreshToken);

        return ResponseEntity.ok(jwtResponse);
    }
}
