package com.okanetransfer.controller;

import com.okanetransfer.dto.request.LoginRequestDTO;
import com.okanetransfer.dto.request.RegisterRequestDTO;
import com.okanetransfer.dto.response.AuthResponseDTO;
import com.okanetransfer.service.AuthService;
import com.okanetransfer.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO dto) {
        log.info("Register request for user: {}", dto.getUsername());
        
        AuthResponseDTO response = authService.register(dto);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "User registered successfully",
                        response
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO dto) {
        log.info("Login request for user: {}", dto.getUsername());
        
        AuthResponseDTO response = authService.login(dto);
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        "Login successful",
                        response
                ));
    }
}
