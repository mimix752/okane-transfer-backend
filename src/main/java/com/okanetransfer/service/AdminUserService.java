package com.okanetransfer.service;

import com.okanetransfer.dto.request.RoleUpdateRequestDTO;
import com.okanetransfer.dto.request.UserRequestDTO;
import com.okanetransfer.dto.response.UserResponseDTO;
import com.okanetransfer.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    Page<UserResponseDTO> getAllUsers(Role role, Boolean active, Pageable pageable);

    UserResponseDTO getUserById(Long id);

    UserResponseDTO createUser(UserRequestDTO dto, String adminIp);

    UserResponseDTO updateUser(Long id, UserRequestDTO dto, String adminIp);

    void toggleUser(Long id, String adminIp);

    void updateRole(Long id, RoleUpdateRequestDTO dto, String adminIp);
}