package com.okanetransfer.dto.response;

import com.okanetransfer.entity.Transfer;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvoiResponseDTO {

    private Long id;
    private String transferCode;
    private String senderName;
    private String recipientName;
    private String recipientCountry;
    private BigDecimal amount;
    private String currency;
    private BigDecimal fees;
    private BigDecimal netAmount;
    private String status;
    private LocalDateTime createdAt;
    private String receipt;

    public static EnvoiResponseDTO fromEntity(Transfer transfer, BigDecimal fees) {
        BigDecimal netAmount = transfer.getAmount().subtract(fees);
        
        return EnvoiResponseDTO.builder()
                .id(transfer.getId())
                .transferCode(transfer.getTransferCode())
                .senderName(transfer.getSender().getUsername())
                .recipientName(transfer.getRecipientName())
                .recipientCountry(transfer.getRecipientCountry())
                .amount(transfer.getAmount())
                .currency(transfer.getCurrency().toString())
                .fees(fees)
                .netAmount(netAmount)
                .status(transfer.getStatus().toString())
                .createdAt(transfer.getCreatedAt())
                .receipt(generateReceipt(transfer, fees, netAmount))
                .build();
    }

    private static String generateReceipt(Transfer transfer, BigDecimal fees, BigDecimal netAmount) {
        return String.format(
                "         REÇU DE TRANSFERT D'ARGENT\n" +
                "Code de retrait: %s\n" +
                "Expéditeur: %s\n" +
                "Bénéficiaire: %s\n" +
                "Montant envoyé: %.2f %s\n" +
                "Frais: %.2f %s\n" +
                "Montant net reçu: %.2f %s\n" +
                "Date: %s\n" +
                "Statut: %s\n",

                transfer.getTransferCode(),
                transfer.getSender().getUsername(),
                transfer.getRecipientName(),
                transfer.getAmount(),
                transfer.getCurrency(),
                fees,
                transfer.getCurrency(),
                netAmount,
                transfer.getCurrency(),
                transfer.getCreatedAt(),
                transfer.getStatus()
        );
    }
}
