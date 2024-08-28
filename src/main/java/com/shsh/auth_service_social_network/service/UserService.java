package com.shsh.auth_service_social_network.service;

import com.shsh.auth_service_social_network.dto.*;
import com.shsh.auth_service_social_network.exceptions.AppError;
import com.shsh.auth_service_social_network.exceptions.PasswordMismatchException;
import com.shsh.auth_service_social_network.security.JwtUtils;
import jakarta.persistence.Id;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.shsh.auth_service_social_network.repository.UserRepository;
import com.shsh.auth_service_social_network.model.User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final IdGenerator idGenerator;
    private final RestTemplate restTemplate;
    public User findByUsername(String email) {
        return userRepository.findByEmail(email);
    }
    @Transactional
    public boolean existsByUsername(String email) {
        return userRepository.existsByEmail(email);
    }
    @Transactional
    public void save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }
    @Transactional
    public ResponseEntity<?> registerNewUser(RegistrationUserDto registrationRequest) {
        try {
            // Проверка на совпадение паролей
            if (!registrationRequest.getPassword().equals(registrationRequest.getConfirmPassword())) {
                throw new PasswordMismatchException("Password and Confirm Password do not match");
            }

            // Создание пользователя
            User user = new User(idGenerator.generateUserId());
            user.setEmail(registrationRequest.getEmail());
            user.setUsername(registrationRequest.getUsername());
            user.setPassword(registrationRequest.getPassword());

            // Сохранение пользователя
            save(user);

            // Создание профиля пользователя
            CreateUserProfileRequest profileRequest = new CreateUserProfileRequest();
            profileRequest.setId(user.getId());
            profileRequest.setEmail(user.getEmail());
            profileRequest.setUsername(user.getUsername());

            try {
                // URL для создания профиля
                String userProfileServiceUrl = "http://USER-PROFILE-SERVICE/user/profile/create";

                // Отправка запроса на создание профиля
                ResponseEntity<Void> profileResponse = restTemplate.postForEntity(userProfileServiceUrl, profileRequest, Void.class);

                if (profileResponse.getStatusCode() != HttpStatus.CREATED) {
                    throw new RuntimeException("Failed to create user profile");
                }
            } catch (Exception e) {
                log.error("Failed to create user profile: {}", e.getMessage());
                // Возвращаем ошибку, если создание профиля не удалось
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new AppError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "User registered but profile creation failed"));
            }

            // Возвращаем успешный ответ
            return ResponseEntity.ok("User registered successfully");
        } catch (PasswordMismatchException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AppError(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Error saving user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AppError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred while saving the user"));
        }
    }

    @Transactional
    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        } catch (BadCredentialsException e) {
            log.error("Invalid login credentials for user: {}", loginRequest.getEmail());
            return new ResponseEntity<>(new AppError(HttpStatus.UNAUTHORIZED.value(), "Invalid credentials"), HttpStatus.UNAUTHORIZED);
        }
        UserDetails userDetails = findByUsername(loginRequest.getEmail());
        User user = findByUsername(loginRequest.getEmail());

        String token = jwtUtils.generateJwtToken(
                user.getEmail(),
                String.valueOf(user.getId())
        );
        String refreshToken = jwtUtils.generateRefreshToken(
                user.getEmail(),
                String.valueOf(user.getId())
        );
        return ResponseEntity.ok(new JwtResponse(token, refreshToken));
    }




}

