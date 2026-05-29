package com.okanetransfer.service;

import com.okanetransfer.dto.request.LoginRequestDTO;
import com.okanetransfer.dto.request.RegisterRequestDTO;
import com.okanetransfer.dto.request.VerifyOtpRequestDTO;
import com.okanetransfer.dto.response.AuthResponseDTO;
import com.okanetransfer.entity.User;
import com.okanetransfer.enums.Role;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.security.JwtTokenProvider;
import com.okanetransfer.security.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private OtpService otpService;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent())
            throw new IllegalArgumentException("Username already exists");
        if (userRepository.findByEmail(dto.getEmail()).isPresent())
            throw new IllegalArgumentException("Email already exists");

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setRole(Role.CLIENT);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(
                savedUser.getId(), savedUser.getUsername(), savedUser.getRole().toString());

        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        response.setUsername(savedUser.getUsername());
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole().toString());
        return response;
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword()))
            throw new IllegalArgumentException("Invalid password");

        if (!user.isEnabled())
            throw new IllegalArgumentException("User account is disabled");

        // 2FA uniquement pour les CLIENTS
        if (user.getRole() == Role.CLIENT) {
            otpService.generateAndSave(user.getUsername());
            AuthResponseDTO response = new AuthResponseDTO();
            response.setToken(null);
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setRole(user.getRole().toString());
            return response;
        }

        // Admin et Agent → token direct sans 2FA
        String token = jwtTokenProvider.generateToken(
                user.getId(), user.getUsername(), user.getRole().toString());

        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().toString());
        return response;
    }

    @Transactional
    public AuthResponseDTO verifyOtp(VerifyOtpRequestDTO dto) {
        if (!otpService.verify(dto.getUsername(), dto.getOtpCode()))
            throw new IllegalArgumentException("Code OTP invalide ou expiré");

        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtTokenProvider.generateToken(
                user.getId(), user.getUsername(), user.getRole().toString());

        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().toString());
        return response;
    }
}