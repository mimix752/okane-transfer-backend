package com.okanetransfer.dto.request;

import jakarta.validation.constraints.NotBlank;

public class RetraitRequestDTO {

    private String transferCode;

    @NotBlank
    private String recipientPhone;

    @NotBlank
    private String recipientCIN;

    public RetraitRequestDTO() {
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

    public String getRecipientCIN() {
        return recipientCIN;
    }

    public void setRecipientCIN(String recipientCIN) {
        this.recipientCIN = recipientCIN;
    }
}
