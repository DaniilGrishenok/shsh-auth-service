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

import java.util.Optional;

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
    public UpdateUserActiveResponse updateUserActive(UpdateUserActiveRequest request) {
        Optional<User> user = userRepository.findById(request.getUserId());

        if (user.isPresent()) {
            User userEntity = user.get();
            userEntity.setActive(request.isSetActiveValue());
            userRepository.save(userEntity);

            UpdateUserActiveResponse response = new UpdateUserActiveResponse();
            response.setMessage("User active status updated successfully.");
            response.setUserId(request.getUserId());
            response.setIsActive(String.valueOf(userEntity.isActive()));
            return response;
        } else {
            throw new IllegalArgumentException("User not found");
        }
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
            validateRegistrationFields(registrationRequest);

            if (!registrationRequest.getPassword().equals(registrationRequest.getConfirmPassword())) {
                throw new PasswordMismatchException("Password and Confirm Password do not match");
            }

            User user = new User(idGenerator.generateUserId());
            user.setEmail(registrationRequest.getEmail().trim());
            user.setUsername(registrationRequest.getUsername().trim());
            user.setPassword(registrationRequest.getPassword());
            user.setActive(true);

            log.info("Registering user: id={}, email={}, username={}",
                    user.getId(), user.getEmail(), user.getUsername());

            if (!createUserProfile(user)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new AppError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "User profile creation failed"));
            }

            save(user);
            return ResponseEntity.ok("User registered successfully");

        } catch (PasswordMismatchException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AppError(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AppError(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Error saving user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AppError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred while saving the user"));
        }
    }

    /**
     * Проверка обязательных полей на null, пустоту, корректность email и пароля.
     */
    private void validateRegistrationFields(RegistrationUserDto registrationRequest) {
        if (registrationRequest == null) {
            throw new IllegalArgumentException("Registration request cannot be null");
        }

        if (isNullOrEmpty(registrationRequest.getEmail()) ||
                isNullOrEmpty(registrationRequest.getUsername()) ||
                isNullOrEmpty(registrationRequest.getPassword()) ||
                isNullOrEmpty(registrationRequest.getConfirmPassword())) {
            throw new IllegalArgumentException("All fields are required and must not be empty");
        }

        // Проверка email на корректность
        if (!isValidEmail(registrationRequest.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Проверка пароля на отсутствие русских символов
        if (!isValidPassword(registrationRequest.getPassword())) {
            throw new IllegalArgumentException("Password must not contain Russian characters");
        }
    }

    /**
     * Утилита для проверки строки на null или пустоту.
     */
    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Проверка корректности email.
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Проверка пароля на отсутствие русских символов.
     */
    private boolean isValidPassword(String password) {
        String russianCharacterRegex = ".*[а-яА-ЯёЁ].*";
        return !password.matches(russianCharacterRegex);
    }

    @Transactional
    public boolean createUserProfile(User user) {

        CreateUserProfileRequest profileRequest = new CreateUserProfileRequest();
        profileRequest.setId(user.getId());
        profileRequest.setEmail(user.getEmail());
        profileRequest.setUsername(user.getRealUsername());

        try {
            String userProfileServiceUrl = "http://USER-PROFILE-SERVICE/user/profile/create";
            log.info("Creating user profile: {}", profileRequest);
            ResponseEntity<Void> profileResponse =
                    restTemplate.postForEntity(
                    userProfileServiceUrl,
                    profileRequest,
                    Void.class);

            if (profileResponse.getStatusCode() == HttpStatus.CREATED) {
                return true;
            } else {
                log.error("Failed to create user profile, received status: {}", profileResponse.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to create user profile: {}", e.getMessage());
            return false;
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
        User user = findByUsername(loginRequest.getEmail());
        if (!user.isActive()) {
            log.warn("User is banned: {}", loginRequest.getEmail());
            return new ResponseEntity<>(new AppError(HttpStatus.UNAUTHORIZED.value(), "Пользователь забанен"), HttpStatus.UNAUTHORIZED);
        }
        String token = jwtUtils.generateJwtToken(
                user.getEmail(),
                String.valueOf(user.getId())
        );
        String refreshToken = jwtUtils.generateRefreshToken(
                user.getEmail(),
                String.valueOf(user.getId())
        );
        return ResponseEntity.ok(new JwtResponse(token, refreshToken, user.getId() ));
    }

}

