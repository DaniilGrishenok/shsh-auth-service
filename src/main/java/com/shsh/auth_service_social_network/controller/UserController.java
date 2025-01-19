package com.shsh.auth_service_social_network.controller;

import com.shsh.auth_service_social_network.dto.LoginRequest;
import com.shsh.auth_service_social_network.dto.UpdateUserActiveRequest;
import com.shsh.auth_service_social_network.dto.UpdateUserActiveResponse;
import com.shsh.auth_service_social_network.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/as/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @PutMapping("/updateUserActive")
    public ResponseEntity<UpdateUserActiveResponse> updateUserActive(@RequestBody UpdateUserActiveRequest request) {
        try {
            UpdateUserActiveResponse response = userService.updateUserActive(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new UpdateUserActiveResponse("User not found", request.getUserId(), "false"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
