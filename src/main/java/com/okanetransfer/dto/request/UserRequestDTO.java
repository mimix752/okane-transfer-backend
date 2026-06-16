package com.okanetransfer.dto.request;

import com.okanetransfer.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class UserRequestDTO {

    @NotBlank
    private String username;

    @NotBlank
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(
            regexp = "^[+]?[0-9]{7,15}$",
            message = "Invalid phone number"
    )
    private String phone;

    private String cin;

    private String country;

    @NotNull
    private Role role;

    public UserRequestDTO() {
    }

    public UserRequestDTO(String username, String email, String phone, String cin, String country, Role role) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.cin = cin;
        this.country = country;
        this.role = role;
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

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
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
}