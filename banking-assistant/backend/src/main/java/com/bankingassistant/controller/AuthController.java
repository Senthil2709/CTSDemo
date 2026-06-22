package com.bankingassistant.controller;

import com.bankingassistant.dto.auth.AuthResponse;
import com.bankingassistant.dto.auth.LoginRequest;
import com.bankingassistant.dto.auth.RegisterRequest;
import com.bankingassistant.dto.auth.UserProfileResponse;
import com.bankingassistant.security.SecurityUser;
import com.bankingassistant.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(@AuthenticationPrincipal SecurityUser principal) {
        return ResponseEntity.ok(userService.getProfile(principal.getId()));
    }
}
