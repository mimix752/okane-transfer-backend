package com.okanetransfer.service.impl;

import com.okanetransfer.dto.request.RoleUpdateRequestDTO;
import com.okanetransfer.dto.request.UserRequestDTO;
import com.okanetransfer.dto.response.UserResponseDTO;
import com.okanetransfer.entity.User;
import com.okanetransfer.enums.Role;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.AdminUserService;
import com.okanetransfer.service.AuditService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    @Override
    public Page<UserResponseDTO> getAllUsers(Role role, Boolean active, Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    @Override
    public UserResponseDTO getUserById(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Transactional
    @Override
    public UserResponseDTO createUser(UserRequestDTO dto, String adminIp) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());
        user.setPassword(passwordEncoder.encode("DefaultPass@123"));

        User saved = userRepository.save(user);

        auditService.logAction(
                adminIp, "CREATE_USER",
                "User", saved.getId(),
                null, saved.getEmail(),
                adminIp
        );

        return toDTO(saved);
    }

    @Transactional
    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO dto, String adminIp) {
        User user = findOrThrow(id);
        String oldEmail = user.getEmail();

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());

        User saved = userRepository.save(user);

        auditService.logAction(
                adminIp, "UPDATE_USER",
                "User", id,
                oldEmail, saved.getEmail(),
                adminIp
        );

        return toDTO(saved);
    }

    @Transactional
    @Override
    public void toggleUser(Long id, String adminIp) {
        User user = findOrThrow(id);
        boolean previousState = user.isEnabled();
        user.setEnabled(!previousState);
        userRepository.save(user);

        auditService.logAction(
                adminIp,
                previousState ? "DISABLE_USER" : "ENABLE_USER",
                "User", id,
                String.valueOf(previousState),
                String.valueOf(!previousState),
                adminIp
        );
    }

    @Transactional
    @Override
    public void updateRole(Long id, RoleUpdateRequestDTO dto, String adminIp) {
        User user = findOrThrow(id);
        Role oldRole = user.getRole();
        user.setRole(dto.getRole());
        userRepository.save(user);

        auditService.logAction(
                adminIp, "UPDATE_ROLE",
                "User", id,
                oldRole.name(),
                dto.getRole().name(),
                adminIp
        );
    }


    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    private UserResponseDTO toDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setActive(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}