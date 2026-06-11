package com.okanetransfer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public class ChangePasswordRequestDTO {

    @NotBlank(message = "Old password is required")
    private String currentPassword;

    @NotBlank(message = "New password tahowa is required")
    @Size(min = 8, message = "password too short")
    private String newPassword;

    public ChangePasswordRequestDTO() {
    }

    public ChangePasswordRequestDTO(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}