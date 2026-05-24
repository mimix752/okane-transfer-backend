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
import com.okanetransfer.repository.CorridorRepository;
import com.okanetransfer.repository.TransferRepository;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.FeeGridService;
import com.okanetransfer.service.NotificationService;
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
    private CorridorRepository corridorRepository;

    @Autowired
    private FeeGridService feeGridService;

    @Autowired
    private TransferCodeService transferCodeService;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public EnvoiResponseDTO createTransfer(EnvoiRequestDTO dto, Long agentId) {

        // 1. Récupérer l'agent (expéditeur)
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));

        // 2. Vérifier que c'est bien un agent
        if (!(agent instanceof Agent)) {
            throw new IllegalArgumentException("User is not an agent");
        }

        // 3. Récupérer le corridor
        Corridor corridor = corridorRepository.findById(dto.getCorridorId())
                .orElseThrow(() -> new ResourceNotFoundException("Corridor not found"));

        // 4. Valider que le corridor correspond aux pays
        validateCorridor(corridor, dto.getSenderCountry(), dto.getRecipientCountry());

        // 5. Calculer les frais
        BigDecimal fees = feeGridService.calculateFee(dto.getCorridorId(), dto.getAmount());

        // 6. Générer le code de retrait unique
        String transferCode = transferCodeService.generateUniqueCode();

        // 7. Créer l'entité Transfer
        Transfer transfer = new Transfer();
        transfer.setTransferCode(transferCode);
        transfer.setSender(agent);
        transfer.setRecipientName(dto.getRecipientName() + " " + dto.getRecipientFirstName());
        transfer.setRecipientPhone(dto.getRecipientPhone());
        transfer.setRecipientCountry(dto.getRecipientCountry());
        transfer.setSenderCountry(dto.getSenderCountry());
        transfer.setAmount(dto.getAmount());
        transfer.setCurrency(Currency.valueOf(dto.getCurrency()));
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setAgency(((Agent) agent).getAgency());
        transfer.setCreatedAt(LocalDateTime.now());

        // 8. Sauvegarder en BD
        Transfer savedTransfer = transferRepository.save(transfer);

        // 9. Envoyer les notifications
        notificationService.sendReceiptBySMS(transfer, fees);
        notificationService.sendReceiptByEmail(transfer, fees, agent.getEmail());

        // 10. Retourner la réponse
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