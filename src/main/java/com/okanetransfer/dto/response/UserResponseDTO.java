package com.okanetransfer.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.okanetransfer.entity.Agent;
import com.okanetransfer.entity.User;
import com.okanetransfer.enums.Role;

import java.time.LocalDateTime;

public class UserResponseDTO {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String country;
    private Role role;
    private boolean active;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private Long   agencyId;
    private String agencyName;

    public UserResponseDTO() {
    }
    public static UserResponseDTO fromEntity(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole());
        dto.setActive(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());

        if (user instanceof Agent) {
            Agent agent = (Agent) user;
            if (agent.getAgency() != null) {
                dto.setAgencyId(agent.getAgency().getId());
                dto.setAgencyName(agent.getAgency().getName());
            }
        }

        return dto;
    }


    public UserResponseDTO(Long id, String username, String email, String phone, String country, Role role, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.country = country;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
    }

    public Long getAgencyId() { return agencyId; }
    public void setAgencyId(Long agencyId) { this.agencyId = agencyId; }

    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}