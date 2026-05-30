package com.okanetransfer.service.envoi;

import com.okanetransfer.dto.request.EnvoiRequestDTO;
import com.okanetransfer.dto.response.EnvoiResponseDTO;
import com.okanetransfer.entity.Agent;
import com.okanetransfer.entity.Corridor;
import com.okanetransfer.entity.Transfer;
import com.okanetransfer.entity.User;
import com.okanetransfer.enums.Currency;
import com.okanetransfer.enums.TransferStatus;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.AgentRepository;
import com.okanetransfer.repository.CorridorRepository;
import com.okanetransfer.repository.TransferRepository;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.AgencyService;
import com.okanetransfer.service.AgentAuditService;
import com.okanetransfer.service.CurrencyConversionService;
import com.okanetransfer.service.FeeGridService;
import com.okanetransfer.service.KycAmlValidationService;
import com.okanetransfer.service.NotificationService;
import com.okanetransfer.service.ReceiptPrintingService;
import com.okanetransfer.service.caisse.CashRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class EnvoiService {

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private CorridorRepository corridorRepository;

    @Autowired
    private FeeGridService feeGridService;

    @Autowired
    private TransferCodeService transferCodeService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AgencyService agencyService;

    @Autowired
    private KycAmlValidationService kycAmlValidationService;

    @Autowired
    private CurrencyConversionService currencyConversionService;

    @Autowired
    private AgentAuditService agentAuditService;

    @Autowired
    private ReceiptPrintingService receiptPrintingService;

    @Autowired
    private CashRegisterService cashRegisterService;

    @Transactional
    public EnvoiResponseDTO createTransfer(EnvoiRequestDTO dto, Long agentId) {

        // 1. Récupérer l'agent directement depuis AgentRepository
        Agent agent = agentRepository.findByUserId(agentId)
                .orElseThrow(() -> new IllegalArgumentException("User is not an agent"));


        // 3. Récupérer le corridor
        Corridor corridor = corridorRepository.findById(dto.getCorridorId())
                .orElseThrow(() -> new ResourceNotFoundException("Corridor not found"));

        // 4. Valider que le corridor correspond aux pays
        validateCorridor(corridor, dto.getSenderCountry(), dto.getRecipientCountry());

        // 5. Validation KYC/AML approfondie
        kycAmlValidationService.validateTransfer(
            dto.getSenderCIN(),
            null, // Pas de CIN requis pour le bénéficiaire
            dto.getAmount(),
            dto.getSenderCountry(),
            dto.getRecipientCountry()
        );

        // 6. Calculer les frais
        BigDecimal fees = feeGridService.calculateFee(dto.getCorridorId(), dto.getAmount());
        BigDecimal totalAmount = dto.getAmount().add(fees);

        // 7. Conversion de devise si nécessaire
        String targetCurrency = corridor.getDestinationCurrency().getCode();
        BigDecimal convertedAmount = dto.getAmount();
        
        if (!dto.getCurrency().equals(targetCurrency)) {
            convertedAmount = currencyConversionService.convertAmount(
                dto.getAmount(), dto.getCurrency(), targetCurrency
            );
        }

        // 8. Vérifier le solde de l'agence
        Long agencyId = ( agent).getAgency().getId();
        agencyService.checkAndDeductBalance(agencyId, totalAmount);

        // 9. Générer le code de retrait unique
        String transferCode = transferCodeService.generateUniqueCode();

        // 10. Créer l'entité Transfer
        Transfer transfer = new Transfer();
        transfer.setTransferCode(transferCode);
        transfer.setSender(agent);
        transfer.setSenderCIN(dto.getSenderCIN());
        transfer.setRecipientName(dto.getRecipientName() + " " + dto.getRecipientFirstName());
        transfer.setRecipientPhone(dto.getRecipientPhone());
        transfer.setRecipientCountry(dto.getRecipientCountry());
        transfer.setSenderCountry(dto.getSenderCountry());
        transfer.setAmount(dto.getAmount());
        transfer.setCurrency(Currency.valueOf(dto.getCurrency()));
        transfer.setFees(fees);
        transfer.setConvertedAmount(convertedAmount);
        transfer.setTargetCurrency(Currency.valueOf(targetCurrency));
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setAgency((agent).getAgency());
        transfer.setCreatedAt(LocalDateTime.now());

        // 11. Sauvegarder en BD
        Transfer savedTransfer = transferRepository.save(transfer);

        // 12. Mettre à jour la caisse (débit)
        try {
            cashRegisterService.crediter(agentId, totalAmount.negate(), "ENVOI", transferCode);
        } catch (Exception ignored) {}

        // 13. Enregistrer dans l'audit trail
        agentAuditService.logTransferCreation(
            savedTransfer.getId(),
            savedTransfer.getTransferCode(),
            savedTransfer.getRecipientName(),
            dto.getAmount().toString(),
            dto.getCurrency()
        );

        // 13. Envoyer les notifications
        notificationService.sendReceiptBySMS(transfer, fees);
        notificationService.sendReceiptByEmail(transfer, fees, agent.getEmail());

        // 14. Imprimer le reçu physique
        String receiptContent = receiptPrintingService.generateTransferReceipt(savedTransfer, fees);
        receiptPrintingService.printReceipt(receiptContent);

        // 15. Retourner la réponse
        return EnvoiResponseDTO.fromEntity(savedTransfer, fees);
    }

    private void validateCorridor(Corridor corridor, String senderCountry, String recipientCountry) {
        if (!corridor.getSourceCountry().equalsIgnoreCase(senderCountry)) {
            throw new IllegalArgumentException(
                    "Sender country does not match corridor source country"
            );
        }
        if (!corridor.getDestinationCountry().equalsIgnoreCase(recipientCountry)) {
            throw new IllegalArgumentException(
                    "Recipient country does not match corridor destination country"
            );
        }
    }
}