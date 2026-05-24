package com.okanetransfer.dto.request;

import jakarta.validation.constraints.NotBlank;

public class RetraitRequestDTO {

    @NotBlank
    private String transferCode;

    @NotBlank
    private String recipientPhone;

    public RetraitRequestDTO() {
    }

    public RetraitRequestDTO(String transferCode, String recipientPhone) {
        this.transferCode = transferCode;
        this.recipientPhone = recipientPhone;
    }

    public String getTransferCode() {
        return transferCode;
    }

    public void setTransferCode(String transferCode) {
        this.transferCode = transferCode;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }
}
