package com.shsh.auth_service_social_network.service;

import com.shsh.auth_service_social_network.dto.JwtResponse;
import com.shsh.auth_service_social_network.dto.UserRegistrationEvent;
import com.shsh.auth_service_social_network.model.User;
import com.shsh.auth_service_social_network.repository.UserRepository;
import com.shsh.auth_service_social_network.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    public ResponseEntity<?> refreshJwtToken(String refreshToken) {
        // Проверка валидности токена
        if (!jwtUtils.validateJwtToken(refreshToken)) {
            log.error("Invalid refresh token: {}", refreshToken);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

        String email = jwtUtils.getEmailFromJwtToken(refreshToken);
        log.info("Extracted email from refresh token: {}", email);

        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.error("User not found for email: {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        // Генерация нового JWT токена
        String newJwtToken = jwtUtils.generateJwtToken(user.getEmail(), String.valueOf(user.getId()));
        JwtResponse jwtResponse = new JwtResponse(newJwtToken, refreshToken, user.getId());

        log.info("New JWT token generated for user: {}", email);

        return ResponseEntity.ok(jwtResponse);
    }



}
