package com.okanetransfer.controller;

import com.okanetransfer.dto.request.LoginRequestDTO;
import com.okanetransfer.dto.request.RegisterRequestDTO;
import com.okanetransfer.dto.response.AuthResponseDTO;
import com.okanetransfer.service.AuthService;
import com.okanetransfer.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO dto) {

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

        AuthResponseDTO response = authService.login(dto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        "Login successful",
                        response
                ));
    }
}