package com.okanetransfer.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class RetraitRequestDTO {

    @NotBlank
    private String transferCode;

    @NotBlank
    private String recipientPhone;
}
