package com.okanetransfer.service.impl;

import com.okanetransfer.dto.request.FeeGridRequestDTO;
import com.okanetransfer.dto.response.FeeGridResponseDTO;
import com.okanetransfer.entity.Corridor;
import com.okanetransfer.entity.FeeGrid;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.CorridorRepository;
import com.okanetransfer.repository.FeeGridRepository;
import com.okanetransfer.service.AuditService;
import com.okanetransfer.service.FeeGridService;
import com.okanetransfer.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeeGridServiceImpl implements FeeGridService {

    private final FeeGridRepository  feeGridRepository;
    private final CorridorRepository corridorRepository;
    private final AuditService       auditService;

    public FeeGridServiceImpl(FeeGridRepository feeGridRepository,
                              CorridorRepository corridorRepository,
                              AuditService auditService) {
        this.feeGridRepository  = feeGridRepository;
        this.corridorRepository = corridorRepository;
        this.auditService       = auditService;
    }

    // ─── Queries ───────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<FeeGridResponseDTO> getByCorridor(Long corridorId) {
        findCorridorOrThrow(corridorId);
        return feeGridRepository
                .findByCorridor_IdAndActive(corridorId, true)
                .stream()
                .map(FeeGridResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FeeGridResponseDTO getById(Long id) {
        return FeeGridResponseDTO.fromEntity(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public FeeGridResponseDTO simulate(Long corridorId,
                                       BigDecimal amount) {
        FeeGrid feeGrid = feeGridRepository
                .findApplicable(corridorId, amount)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No fee grid found for amount "
                                + amount + " on corridor " + corridorId));

        FeeGridResponseDTO dto = FeeGridResponseDTO.fromEntity(feeGrid);
        dto.setSimulatedFeeForMaxAmount(computeFee(feeGrid, amount));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateFee(Long corridorId,
                                   BigDecimal amount) {
        FeeGrid feeGrid = feeGridRepository
                .findApplicable(corridorId, amount)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No fee grid found for amount " + amount));
        return computeFee(feeGrid, amount);
    }

    // ─── Commands ──────────────────────────────────────────────

    @Override
    @Transactional
    public FeeGridResponseDTO create(FeeGridRequestDTO dto,
                                     String adminIp) {
        Corridor corridor = findCorridorOrThrow(dto.getCorridorId());

        validateAmountRange(dto.getMinAmount(), dto.getMaxAmount());
        validateShares(dto.getAgencyShare(), dto.getCentralShare());

        if (feeGridRepository.existsOverlap(
                dto.getCorridorId(),
                dto.getMinAmount(),
                dto.getMaxAmount(), -1L))
            throw new IllegalArgumentException(
                    "Amount range ["
                            + dto.getMinAmount() + " - " + dto.getMaxAmount()
                            + "] overlaps with an existing fee grid");

        FeeGrid feeGrid = FeeGrid.builder()
                .corridor(corridor)
                .minAmount(dto.getMinAmount())
                .maxAmount(dto.getMaxAmount())
                .fixedFee(dto.getFixedFee())
                .percentageFee(dto.getPercentageFee())
                .agencyShare(dto.getAgencyShare())
                .centralShare(dto.getCentralShare())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();

        FeeGrid saved = feeGridRepository.save(feeGrid);

        auditService.log(
                SecurityUtils.getCurrentUsername(),
                "CREATE_FEEGRID",
                "FeeGrid",
                saved.getId(),
                "corridorId=" + dto.getCorridorId()
                        + " | range=[" + dto.getMinAmount()
                        + "-" + dto.getMaxAmount() + "]"
                        + " | fixedFee=" + dto.getFixedFee()
                        + " | percentageFee=" + dto.getPercentageFee()
                        + " | agencyShare=" + dto.getAgencyShare()
                        + " | centralShare=" + dto.getCentralShare()
                        + " | ip=" + adminIp
        );

        return FeeGridResponseDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public FeeGridResponseDTO update(Long id,
                                     FeeGridRequestDTO dto,
                                     String adminIp) {
        FeeGrid  feeGrid  = findOrThrow(id);
        Corridor corridor = findCorridorOrThrow(dto.getCorridorId());

        validateAmountRange(dto.getMinAmount(), dto.getMaxAmount());
        validateShares(dto.getAgencyShare(), dto.getCentralShare());

        if (feeGridRepository.existsOverlap(
                dto.getCorridorId(),
                dto.getMinAmount(),
                dto.getMaxAmount(), id))
            throw new IllegalArgumentException(
                    "Amount range overlaps with existing fee grid");

        // Snapshot avant modification
        String oldRange         = "[" + feeGrid.getMinAmount()
                + "-" + feeGrid.getMaxAmount() + "]";
        String oldFixedFee      = String.valueOf(feeGrid.getFixedFee());
        String oldPercentageFee = String.valueOf(feeGrid.getPercentageFee());
        String oldAgencyShare   = String.valueOf(feeGrid.getAgencyShare());
        String oldCentralShare  = String.valueOf(feeGrid.getCentralShare());

        feeGrid.setCorridor(corridor);
        feeGrid.setMinAmount(dto.getMinAmount());
        feeGrid.setMaxAmount(dto.getMaxAmount());
        feeGrid.setFixedFee(dto.getFixedFee());
        feeGrid.setPercentageFee(dto.getPercentageFee());
        feeGrid.setAgencyShare(dto.getAgencyShare());
        feeGrid.setCentralShare(dto.getCentralShare());
        if (dto.getActive() != null)
            feeGrid.setActive(dto.getActive());

        FeeGrid updated = feeGridRepository.save(feeGrid);

        auditService.log(
                SecurityUtils.getCurrentUsername(),
                "UPDATE_FEEGRID",
                "FeeGrid",
                id,
                "old=[range=" + oldRange
                        + ", fixedFee=" + oldFixedFee
                        + ", percentageFee=" + oldPercentageFee
                        + ", agencyShare=" + oldAgencyShare
                        + ", centralShare=" + oldCentralShare + "]"
                        + " | new=[range=[" + updated.getMinAmount()
                        + "-" + updated.getMaxAmount() + "]"
                        + ", fixedFee=" + updated.getFixedFee()
                        + ", percentageFee=" + updated.getPercentageFee()
                        + ", agencyShare=" + updated.getAgencyShare()
                        + ", centralShare=" + updated.getCentralShare() + "]"
                        + " | ip=" + adminIp
        );

        return FeeGridResponseDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public void toggle(Long id, String adminIp) {
        FeeGrid feeGrid  = findOrThrow(id);
        boolean previous = feeGrid.isActive();
        feeGrid.setActive(!previous);
        feeGridRepository.save(feeGrid);

        auditService.log(
                SecurityUtils.getCurrentUsername(),
                previous ? "DEACTIVATE_FEEGRID" : "ACTIVATE_FEEGRID",
                "FeeGrid",
                id,
                "old=" + previous
                        + " | new=" + !previous
                        + " | corridorId=" + feeGrid.getCorridor().getId()
                        + " | range=[" + feeGrid.getMinAmount()
                        + "-" + feeGrid.getMaxAmount() + "]"
                        + " | ip=" + adminIp
        );
    }

    // ─── Helpers privés ────────────────────────────────────────

    private BigDecimal computeFee(FeeGrid grid, BigDecimal amount) {
        BigDecimal percentPart = amount
                .multiply(grid.getPercentageFee())
                .divide(BigDecimal.valueOf(100), 2,
                        RoundingMode.HALF_UP);
        return grid.getFixedFee()
                .add(percentPart)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void validateAmountRange(BigDecimal min, BigDecimal max) {
        if (min.compareTo(max) >= 0)
            throw new IllegalArgumentException(
                    "minAmount must be strictly less than maxAmount");
    }

    private void validateShares(BigDecimal agencyShare,
                                BigDecimal centralShare) {
        BigDecimal total = agencyShare.add(centralShare);
        if (total.compareTo(BigDecimal.valueOf(100)) != 0)
            throw new IllegalArgumentException(
                    "agencyShare + centralShare must equal 100. Got: "
                            + total);
    }

    private FeeGrid findOrThrow(Long id) {
        return feeGridRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "FeeGrid not found with id: " + id));
    }

    private Corridor findCorridorOrThrow(Long id) {
        return corridorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Corridor not found with id: " + id));
    }
}