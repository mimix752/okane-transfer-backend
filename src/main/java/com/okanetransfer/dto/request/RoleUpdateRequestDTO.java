package com.okanetransfer.dto.request;

import com.okanetransfer.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleUpdateRequestDTO {

    @NotNull
    private Role role;
}