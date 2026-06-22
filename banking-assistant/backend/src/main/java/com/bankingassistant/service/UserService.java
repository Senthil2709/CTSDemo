package com.bankingassistant.service;

import com.bankingassistant.dto.auth.AuthResponse;
import com.bankingassistant.dto.auth.LoginRequest;
import com.bankingassistant.dto.auth.RegisterRequest;
import com.bankingassistant.dto.auth.UserProfileResponse;
import com.bankingassistant.entity.AccountTier;
import com.bankingassistant.entity.KycDetail;
import com.bankingassistant.entity.KycStatus;
import com.bankingassistant.entity.Role;
import com.bankingassistant.entity.User;
import com.bankingassistant.repository.KycDetailRepository;
import com.bankingassistant.repository.UserRepository;
import com.bankingassistant.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Module 1 - User Authentication and Authorization.
 * Handles customer/staff registration, login (JWT issuance), and profile
 * retrieval including KYC status.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final KycDetailRepository kycDetailRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @org.springframework.beans.factory.annotation.Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .role(Role.CUSTOMER)
                .accountTier(AccountTier.STANDARD)
                .enabled(true)
                .build();
        user = userRepository.save(user);

        KycDetail kyc = KycDetail.builder()
                .userId(user.getId())
                .kycStatus(KycStatus.PENDING)
                .build();
        kycDetailRepository.save(kyc);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .expiresInMs(jwtExpirationMs)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (Exception ex) {
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .expiresInMs(jwtExpirationMs)
                .build();
    }

    public UserProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        KycDetail kyc = kycDetailRepository.findByUserId(userId).orElse(null);

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .accountTier(user.getAccountTier().name())
                .kycStatus(kyc == null ? KycStatus.PENDING.name() : kyc.getKycStatus().name())
                .build();
    }
}
