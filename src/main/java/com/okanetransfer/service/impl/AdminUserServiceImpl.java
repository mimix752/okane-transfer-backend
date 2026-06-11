package com.okanetransfer.service.impl;

import com.okanetransfer.dto.request.RoleUpdateRequestDTO;
import com.okanetransfer.dto.request.UserRequestDTO;
import com.okanetransfer.dto.response.UserResponseDTO;
import com.okanetransfer.entity.User;
import com.okanetransfer.enums.Role;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.AdminUserService;
import com.okanetransfer.service.AuditService;
import com.okanetransfer.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Transactional(readOnly = true)
    @Override
    public Page<UserResponseDTO> getAllUsers(Role role, Boolean active,
                                             Pageable pageable) {
        if (role != null && active != null)
            return userRepository.findByRoleAndEnabled(role, active, pageable)
                    .map(this::toDTO);
        if (role != null)
            return userRepository.findByRole(role, pageable).map(this::toDTO);
        if (active != null)
            return userRepository.findByEnabled(active, pageable).map(this::toDTO);

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

        auditService.log(
                SecurityUtils.getCurrentUsername(),
                "CREATE_USER",
                "User",
                saved.getId(),
                LocalDateTime.now()+" - Utilisateur créé avec email : " + saved.getEmail()
                        + " dont le role est : " + saved.getRole()
        );

        return toDTO(saved);
    }

    @Transactional
    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO dto,
                                      String adminIp) {
        User user = findOrThrow(id);

        String oldEmail    = user.getEmail();
        String oldUsername = user.getUsername();
        Role   oldRole     = user.getRole();

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());

        User saved = userRepository.save(user);

        auditService.log(
                SecurityUtils.getCurrentUsername(),
                "UPDATE_USER",
                "User",
                id,
                LocalDateTime.now()+" - Modification de L'utilisateur "+ saved.getId() + " old infos : "
                        + "username : " +  oldUsername
                        + ", email : " + oldEmail
                        + ", role : " + oldRole
                        + " | New infos : username :" + saved.getUsername()
                        + ", email : " + saved.getEmail()
                        + ", role : " + saved.getRole()
        );

        return toDTO(saved);
    }

    @Transactional
    @Override
    public void toggleUser(Long id, String adminIp) {

        User user = findOrThrow(id);
        boolean previous = user.isEnabled();
        user.setEnabled(!previous);
        userRepository.save(user);

        String oldStatus = previous ? "Désactivé" : "Activé";
        String newStatus = !previous ? "Désactivé" : "Activé";

        auditService.log(
                SecurityUtils.getCurrentUsername(),
                previous ? "DISABLE_USER" : "ENABLE_USER",
                "User",
                id,
                LocalDateTime.now() + " - Modification de status de l'utilisateur old : " + oldStatus
                        + " -> new : " + newStatus
        );
    }

    @Transactional
    @Override
    public void updateRole(Long id, RoleUpdateRequestDTO dto,
                           String adminIp) {
        User user = findOrThrow(id);
        Role oldRole = user.getRole();
        user.setRole(dto.getRole());
        userRepository.save(user);

        auditService.log(
                SecurityUtils.getCurrentUsername(),
                "UPDATE_ROLE",
                "User",
                id,
                LocalDateTime.now() + " - Modification de role de L#utilisateur old=" + oldRole.name()
                        + " -> new=" + dto.getRole().name()
        );
    }


    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found with id: " + id));
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

