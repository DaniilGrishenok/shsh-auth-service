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

    @PostMapping("/registration")
    public ResponseEntity<?> register(@RequestBody RegistrationUserDto registrationRequest) {
        return userService.registerNewUser(registrationRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody JwtRequest authRequest) {
        return userService.authenticateUser(authRequest);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<AppError> handlePasswordMismatchException(PasswordMismatchException ex) {
        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}