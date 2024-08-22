package com.shsh.auth_service_social_network.controller;

import com.shsh.auth_service_social_network.dto.JwtRequest;
import com.shsh.auth_service_social_network.dto.JwtResponse;
import com.shsh.auth_service_social_network.dto.RegistrationUserDto;
import com.shsh.auth_service_social_network.dto.UserRegistrationEvent;
import com.shsh.auth_service_social_network.exceptions.AppError;
import com.shsh.auth_service_social_network.exceptions.PasswordMismatchException;
import com.shsh.auth_service_social_network.model.User;
import com.shsh.auth_service_social_network.security.JwtUtils;
import com.shsh.auth_service_social_network.service.KafkaProducer;
import com.shsh.auth_service_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final KafkaProducer kafkaProducer;
    @PostMapping("/registration")
    public ResponseEntity<?> register(@RequestBody RegistrationUserDto registrationRequest) {
        try {
            if (!registrationRequest.getPassword().equals(registrationRequest.getConfirmPassword())) {
                throw new PasswordMismatchException("Password and Confirm Password do not match");
            }
            try {
                User user = new User();
                user.setEmail(registrationRequest.getEmail());
                user.setUsername(registrationRequest.getUsername());
                user.setPassword(registrationRequest.getPassword());

                userService.save(user);
                UserRegistrationEvent event = new UserRegistrationEvent(user.getId(), user.getUsername(), user.getEmail());
                kafkaProducer.sendMessage(event);
            } catch (Exception e) {
                log.error("Error saving user: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new AppError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred while saving the user"));
            }
        } catch (PasswordMismatchException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AppError(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody JwtRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        } catch (BadCredentialsException e) {
            log.error("Invalid login credentials for user: {}", authRequest.getUsername());
            return new ResponseEntity<>(new AppError(HttpStatus.UNAUTHORIZED.value(), "Invalid credentials"), HttpStatus.UNAUTHORIZED);
        }

        UserDetails userDetails = userService.findByUsername(authRequest.getUsername());
        User user = userService.findByUsername(authRequest.getUsername());
        String token = jwtUtils.generateJwtToken(userDetails.getUsername(), user.getEmail(), String.valueOf(user.getId()));

        return ResponseEntity.ok(new JwtResponse(token));
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<AppError> handlePasswordMismatchException(PasswordMismatchException ex) {
        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), ex.getMessage()), HttpStatus.BAD_REQUEST);
    }



}
