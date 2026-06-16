package com.okanetransfer.dto.request;

import com.okanetransfer.enums.MobileMoneyOperator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class MobileMoneyRequestDTO {

    @NotNull
    private Long transferId;

    @NotNull
    private MobileMoneyOperator operator;

    @NotBlank
    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Invalid mobile account format")
    private String mobileAccount;

    public Long getTransferId() { return transferId; }
    public void setTransferId(Long transferId) { this.transferId = transferId; }
    public MobileMoneyOperator getOperator() { return operator; }
    public void setOperator(MobileMoneyOperator operator) { this.operator = operator; }
    public String getMobileAccount() { return mobileAccount; }
    public void setMobileAccount(String mobileAccount) { this.mobileAccount = mobileAccount; }
}
