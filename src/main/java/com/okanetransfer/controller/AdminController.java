package com.okanetransfer.controller;

import com.okanetransfer.dto.request.RoleUpdateRequestDTO;
import com.okanetransfer.dto.request.UserRequestDTO;
import com.okanetransfer.dto.response.UserResponseDTO;
import com.okanetransfer.enums.Role;
import com.okanetransfer.service.AdminUserService;
import com.okanetransfer.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Users", description = "User management endpoints for administrators")
public class AdminController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Returns paginated list of users filterable by role and status")
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> getAllUsers(
            @Parameter(description = "Filter by role") @RequestParam(required = false) Role role,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean active,
            Pageable pageable) {

        Page<UserResponseDTO> result = adminUserService.getAllUsers(role, active, pageable);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", result));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {

        UserResponseDTO result = adminUserService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", result));
    }

    @PostMapping("/users")
    @Operation(summary = "Create a new user")
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(
            @Valid @RequestBody UserRequestDTO dto,
            HttpServletRequest request) {

        UserResponseDTO result = adminUserService.createUser(dto, request.getRemoteAddr());
        return ResponseEntity.status(201).body(ApiResponse.success("User created successfully", result));
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Update an existing user")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO dto,
            HttpServletRequest request) {

        UserResponseDTO result = adminUserService.updateUser(id, dto, request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", result));
    }

    @PatchMapping("/users/{id}/toggle")
    @Operation(summary = "Toggle user active status")
    public ResponseEntity<ApiResponse<Void>> toggleUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            HttpServletRequest request) {

        adminUserService.toggleUser(id, request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("User status toggled successfully", null));
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Update user role")
    public ResponseEntity<ApiResponse<Void>> updateRole(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequestDTO dto,
            HttpServletRequest request) {

        adminUserService.updateRole(id, dto, request.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully", null));
    }
}