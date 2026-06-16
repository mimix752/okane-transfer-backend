package com.okanetransfer.dto.request;


public class RetraitRequestDTO {

    private String transferCode;

    private String senderPhone;
    private String senderCIN;

    public RetraitRequestDTO() {
    }

    public String getTransferCode() {
        return transferCode;
    }

    public void setTransferCode(String transferCode) {
        this.transferCode = transferCode;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public String getSenderCIN() {
        return senderCIN;
    }

    public void setSenderCIN(String senderCIN) {
        this.senderCIN = senderCIN;
    }
}
