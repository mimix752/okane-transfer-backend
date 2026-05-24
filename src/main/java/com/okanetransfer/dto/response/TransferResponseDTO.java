package com.okanetransfer.dto.response;

import com.okanetransfer.entity.Transfer;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponseDTO {

    private Long id;
    private String transferCode;
    private String senderUsername;
    private String recipientName;
    private String recipientPhone;
    private BigDecimal amount;
    private String currency;
    private BigDecimal convertedAmount;
    private String targetCurrency;
    private String status;
    private String agencyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TransferResponseDTO fromEntity(Transfer transfer) {
        return TransferResponseDTO.builder()
                .id(transfer.getId())
                .transferCode(transfer.getTransferCode())
                .senderUsername(transfer.getSender().getUsername())
                .recipientName(transfer.getRecipientName())
                .recipientPhone(transfer.getRecipientPhone())
                .amount(transfer.getAmount())
                .currency(transfer.getCurrency() != null ? transfer.getCurrency().toString() : null)
                .convertedAmount(transfer.getConvertedAmount())
                .targetCurrency(transfer.getTargetCurrency() != null ? transfer.getTargetCurrency().toString() : null)
                .status(transfer.getStatus().toString())
                .agencyName(transfer.getAgency() != null ? transfer.getAgency().getName() : null)
                .createdAt(transfer.getCreatedAt())
                .updatedAt(transfer.getUpdatedAt())
                .build();
    }
}
