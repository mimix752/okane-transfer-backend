package com.okanetransfer.dto.request;

import com.okanetransfer.enums.Role;
import jakarta.validation.constraints.NotNull;

public class RoleUpdateRequestDTO {

    @NotNull
    private Role role;

    public RoleUpdateRequestDTO() {}

    public RoleUpdateRequestDTO(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

}