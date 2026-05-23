package com.okanetransfer.dto.response;

import com.okanetransfer.enums.TransferStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RetraitResponseDTO {
    private Long id;
    private String transferCode;
    private String recipientName;
    private BigDecimal paidAmount;
    private TransferStatus status;
    private LocalDateTime paidAt;
}
