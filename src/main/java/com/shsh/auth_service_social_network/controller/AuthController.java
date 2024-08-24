package com.shsh.auth_service_social_network.controller;

import com.shsh.auth_service_social_network.dto.LoginRequest;
import com.shsh.auth_service_social_network.dto.RegistrationUserDto;
import com.shsh.auth_service_social_network.exceptions.AppError;
import com.shsh.auth_service_social_network.exceptions.PasswordMismatchException;
import com.shsh.auth_service_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return userService.authenticateUser(loginRequest);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<AppError> handlePasswordMismatchException(PasswordMismatchException ex) {
        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}