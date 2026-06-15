package com.okanetransfer.service.envoi;

import com.okanetransfer.dto.request.EnvoiRequestDTO;
import com.okanetransfer.dto.response.EnvoiResponseDTO;
import com.okanetransfer.dto.response.PageResponse;
import com.okanetransfer.entity.Agent;
import com.okanetransfer.entity.Corridor;
import com.okanetransfer.entity.Currency;
import com.okanetransfer.entity.Transfer;
import com.okanetransfer.enums.TransferStatus;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.AgentRepository;
import com.okanetransfer.repository.CorridorRepository;
import com.okanetransfer.repository.CurrencyRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnvoiService {

    @Autowired private TransferRepository transferRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AgentRepository agentRepository;
    @Autowired private CorridorRepository corridorRepository;
    @Autowired private FeeGridService feeGridService;
    @Autowired private TransferCodeService transferCodeService;
    @Autowired private NotificationService notificationService;
    @Autowired private AgencyService agencyService;
    @Autowired private KycAmlValidationService kycAmlValidationService;
    @Autowired private CurrencyConversionService currencyConversionService;
    @Autowired private AgentAuditService agentAuditService;
    @Autowired private ReceiptPrintingService receiptPrintingService;
    @Autowired private CurrencyRepository currencyRepository;
    @Autowired private CashRegisterService cashRegisterService;

    @Transactional
    public EnvoiResponseDTO createTransfer(EnvoiRequestDTO dto, Long agentId) {

        Agent agent = agentRepository.findByUserId(agentId)
                .orElseThrow(() -> new IllegalArgumentException("User is not an agent"));

        Corridor corridor = corridorRepository.findById(dto.getCorridorId())
                .orElseThrow(() -> new ResourceNotFoundException("Corridor not found"));
        validateCorridor(corridor, dto.getSenderCountry(), dto.getRecipientCountry());

        try {
            String recipientDoc = (dto.getRecipientCIN() != null && !dto.getRecipientCIN().isBlank())
                    ? dto.getRecipientCIN() : null;
            kycAmlValidationService.validateTransfer(
                    dto.getSenderCIN(), recipientDoc, dto.getAmount(),
                    dto.getSenderCountry(), dto.getRecipientCountry());
        } catch (Exception e) {
            System.out.println("KYC/AML warning (non bloquant): " + e.getMessage());
        }

        BigDecimal fees = feeGridService.calculateFee(dto.getCorridorId(), dto.getAmount());
        BigDecimal totalAmount = dto.getAmount().add(fees);

        Currency sourceCurrency = currencyRepository.findById(dto.getCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found"));
        Currency targetCurrency = corridor.getDestinationCurrency();
        BigDecimal convertedAmount = dto.getAmount();
        if (!sourceCurrency.getCode().equals(targetCurrency.getCode())) {
            convertedAmount = currencyConversionService.convertAmount(
                    dto.getAmount(), sourceCurrency.getCode(), targetCurrency.getCode());
        }

        Long agencyId = agent.getAgency().getId();
        agencyService.checkAndDeductBalance(agencyId, totalAmount);

        String transferCode = transferCodeService.generateUniqueCode();
        Transfer transfer = new Transfer();
        transfer.setTransferCode(transferCode);
        transfer.setSender(agent);
        transfer.setSenderCIN(dto.getSenderCIN());
        transfer.setRecipientName(dto.getRecipientName() + " " + dto.getRecipientFirstName());
        transfer.setRecipientPhone(dto.getRecipientPhone());
        transfer.setRecipientCountry(dto.getRecipientCountry());
        transfer.setSenderCountry(dto.getSenderCountry());
        transfer.setAmount(dto.getAmount());
        transfer.setCurrency(sourceCurrency);
        transfer.setFees(fees);
        transfer.setConvertedAmount(convertedAmount);
        transfer.setTargetCurrency(targetCurrency);
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setAgency(agent.getAgency());
        transfer.setCreatedAt(LocalDateTime.now());

        Transfer savedTransfer = transferRepository.save(transfer);

        try { cashRegisterService.crediter(agentId, totalAmount.negate(), "ENVOI", transferCode); } catch (Exception ignored) {}
        try { agentAuditService.logTransferCreation(savedTransfer.getId(), savedTransfer.getTransferCode(), savedTransfer.getRecipientName(), dto.getAmount().toString(), sourceCurrency.getCode()); } catch (Exception ignored) {}
        try { notificationService.sendReceiptBySMS(transfer, fees); } catch (Exception ignored) {}
        try { notificationService.sendReceiptByEmail(transfer, fees, agent.getEmail()); } catch (Exception ignored) {}
        try {
            String receipt = receiptPrintingService.generateTransferReceipt(savedTransfer, fees);
            receiptPrintingService.printReceipt(receipt);
        } catch (Exception ignored) {}

        return EnvoiResponseDTO.fromEntity(savedTransfer, fees);
    }

    private void validateCorridor(Corridor corridor, String senderCountry, String recipientCountry) {
        if (!corridor.getSourceCountry().equalsIgnoreCase(senderCountry))
            throw new IllegalArgumentException("Sender country does not match corridor source country");
        if (!corridor.getDestinationCountry().equalsIgnoreCase(recipientCountry))
            throw new IllegalArgumentException("Recipient country does not match corridor destination country");
    }

    @Transactional(readOnly = true)
    public PageResponse<EnvoiResponseDTO> getRecentTransfersPaginated(Long agentId, int page, int size) {
        Agent agent = agentRepository.findByUserId(agentId)
                .orElseThrow(() -> new IllegalArgumentException("User is not an agent"));
        
        Pageable pageable = PageRequest.of(page, size);
        var transferPage = transferRepository.findByAgencyIdOrderByCreatedAtDesc(agent.getAgency().getId(), pageable);
        
        List<EnvoiResponseDTO> content = transferPage.getContent().stream()
                .map(t -> EnvoiResponseDTO.fromEntity(t, t.getFees()))
                .collect(Collectors.toList());
        
        return new PageResponse<>(content, page, size, transferPage.getTotalElements(), 
                transferPage.getTotalPages(), transferPage.isFirst(), transferPage.isLast());
    }

    @Transactional(readOnly = true)
    public EnvoiResponseDTO searchTransferByCode(String code) {
        Transfer transfer = transferRepository.findByTransferCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer not found with code: " + code));
        return EnvoiResponseDTO.fromEntity(transfer, transfer.getFees());
    }
