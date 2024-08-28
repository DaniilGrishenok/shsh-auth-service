package com.shsh.auth_service_social_network.service;

import com.shsh.auth_service_social_network.dto.JwtResponse;
import com.shsh.auth_service_social_network.dto.LoginRequest;
import com.shsh.auth_service_social_network.dto.RegistrationUserDto;
import com.shsh.auth_service_social_network.dto.UserRegistrationEvent;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final IdGenerator idGenerator;

    public User findByUsername(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByUsername(String email) {
        return userRepository.existsByEmail(email);
    }

    public void save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public ResponseEntity<?> registerNewUser(RegistrationUserDto registrationRequest) {
        try {
            if (!registrationRequest.getPassword().equals(registrationRequest.getConfirmPassword())) {
                throw new PasswordMismatchException("Password and Confirm Password do not match");
            }
            User user = new User(idGenerator.generateUserId());
            user.setEmail(registrationRequest.getEmail());
            user.setUsername(registrationRequest.getUsername());
            user.setPassword(registrationRequest.getPassword());

            save(user);


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

