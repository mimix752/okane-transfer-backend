package com.okanetransfer.service.impl;

import com.okanetransfer.dto.request.AgencyRequestDTO;
import com.okanetransfer.dto.response.AgencyPerformanceResponseDTO;
import com.okanetransfer.dto.response.AgencyResponseDTO;
import com.okanetransfer.entity.Agency;
import com.okanetransfer.entity.Agent;
import com.okanetransfer.entity.Transfer;
import com.okanetransfer.entity.User;
import com.okanetransfer.enums.Role;
import com.okanetransfer.enums.TransferStatus;
import com.okanetransfer.exception.InsufficientFundsException;
import com.okanetransfer.repository.AgencyRepository;
import com.okanetransfer.repository.AgentRepository;
import com.okanetransfer.repository.TransferRepository;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.AgencyService;
import com.okanetransfer.service.AuditService;
import com.okanetransfer.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgencyServiceImpl implements AgencyService {

    @Autowired private AgencyRepository agencyRepository;
    @Autowired private AgentRepository agentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AuditService auditService;
    @Autowired private TransferRepository transferRepository;

    // ─── Queries ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    @Override
    public List<AgencyResponseDTO> getAllAgencies(String country, Boolean active) {
        List<Agency> agencies;
        if (country != null && active != null) {
            agencies = agencyRepository.findByCountry(country).stream()
                    .filter(a -> a.isActive() == active).collect(Collectors.toList());
        } else if (country != null) {
            agencies = agencyRepository.findByCountry(country);
        } else if (active != null) {
            agencies = agencyRepository.findByActive(active);
        } else {
            agencies = agencyRepository.findAll();
        }
        return agencies.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public AgencyResponseDTO getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    // ─── Commands ──────────────────────────────────────────────

    @Transactional
    @Override
    public AgencyResponseDTO create(AgencyRequestDTO dto, String adminIp) {
        Agency agency = new Agency();
        agency.setName(dto.getName());
        agency.setAddress(dto.getAddress());
        agency.setCountry(dto.getCountry());
        agency.setDailyLimit(dto.getDailyLimit());
        agency.setCurrentBalance(BigDecimal.ZERO);
        agency.setActive(true);

        Agency saved = agencyRepository.save(agency);
        auditService.log(SecurityUtils.getCurrentUsername(), "CREATE_AGENCY", "Agency", saved.getId(),
                "name=" + saved.getName() + " | country=" + saved.getCountry() + " | dailyLimit=" + saved.getDailyLimit());
        return toDTO(saved);
    }

    @Transactional
    @Override
    public AgencyResponseDTO update(Long id, AgencyRequestDTO dto, String adminIp) {
        Agency agency = findOrThrow(id);
        String oldName = agency.getName();
        String oldCountry = agency.getCountry();
        BigDecimal oldDailyLimit = agency.getDailyLimit();

        agency.setName(dto.getName());
        agency.setAddress(dto.getAddress());
        agency.setCountry(dto.getCountry());
        agency.setDailyLimit(dto.getDailyLimit());

        Agency saved = agencyRepository.save(agency);
        auditService.log(SecurityUtils.getCurrentUsername(), "UPDATE_AGENCY", "Agency", id,
                "old name=" + oldName + ", old country=" + oldCountry + ", old dailyLimit=" + oldDailyLimit
                + " -> name=" + saved.getName() + ", country=" + saved.getCountry() + ", dailyLimit=" + saved.getDailyLimit());
        return toDTO(saved);
    }

    @Transactional
    @Override
    public void toggle(Long id, String adminIp) {
        Agency agency = findOrThrow(id);
        boolean previous = agency.isActive();
        agency.setActive(!previous);
        agencyRepository.save(agency);
        auditService.log(SecurityUtils.getCurrentUsername(),
                previous ? "DISABLE_AGENCY" : "ENABLE_AGENCY", "Agency", id,
                previous + " -> " + !previous + " | ip=" + adminIp);
    }

    @Transactional
    @Override
    public void addAgent(Long agencyId, Long userId, String adminIp) {
        Agency agency = findOrThrow(agencyId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        user.setRole(Role.AGENT);
        userRepository.save(user);

        List<Agent> existing = agentRepository.findByAgency_Id(agencyId).stream()
                .filter(a -> a.getId().equals(userId)).collect(Collectors.toList());

        if (existing.isEmpty()) {
            Agent agent = new Agent();
            agent.setId(user.getId());
            agent.setUsername(user.getUsername());
            agent.setEmail(user.getEmail());
            agent.setPassword(user.getPassword());
            agent.setPhone(user.getPhone());
            agent.setRole(Role.AGENT);
            agent.setEnabled(user.isEnabled());
            agent.setAgency(agency);
            agent.setActive(true);
            agentRepository.save(agent);
        } else {
            Agent agent = existing.get(0);
            agent.setAgency(agency);
            agent.setActive(true);
            agentRepository.save(agent);
        }

        auditService.log(SecurityUtils.getCurrentUsername(), "ADD_AGENT", "Agency", agencyId,
                "userId=" + userId + " | username=" + user.getUsername() + " | ip=" + adminIp);
    }

    @Transactional
    @Override
    public void removeAgent(Long agencyId, Long userId, String adminIp) {
        Agent agent = agentRepository.findByAgency_Id(agencyId).stream()
                .filter(a -> a.getId().equals(userId)).findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Agent not found for agencyId=" + agencyId + " userId=" + userId));

        String username = agent.getUsername();
        agent.setActive(false);
        agentRepository.save(agent);
        auditService.log(SecurityUtils.getCurrentUsername(), "REMOVE_AGENT", "Agency", agencyId,
                "userId=" + userId + " | username=" + username + " | ip=" + adminIp);
    }

    // ─── Performance ──────────────────────────────────────────

    @Transactional(readOnly = true)
    @Override
    public AgencyPerformanceResponseDTO getPerformance(Long id) {
        return buildPerformance(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    @Override
    public List<AgencyPerformanceResponseDTO> getAllPerformances() {
        return agencyRepository.findByActive(true).stream()
                .map(this::buildPerformance)
                .sorted((a, b) -> b.getMonthlyVolume().compareTo(a.getMonthlyVolume()))
                .collect(Collectors.toList());
    }

    private AgencyPerformanceResponseDTO buildPerformance(Agency agency) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        List<Transfer> monthlyTransfers = transferRepository
                .findByAgencyIdAndCreatedAtBetween(agency.getId(), startOfMonth, now);
        List<Transfer> dailyTransfers = monthlyTransfers.stream()
                .filter(t -> t.getCreatedAt() != null && !t.getCreatedAt().isBefore(startOfDay))
                .collect(Collectors.toList());

        BigDecimal dailyVolume = dailyTransfers.stream().map(Transfer::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal monthlyVolume = monthlyTransfers.stream().map(Transfer::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal dailyFees = dailyTransfers.stream()
                .map(t -> t.getFees() != null ? t.getFees() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal monthlyFees = monthlyTransfers.stream()
                .map(t -> t.getFees() != null ? t.getFees() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal usedPercent = BigDecimal.ZERO;
        if (agency.getDailyLimit() != null && agency.getDailyLimit().compareTo(BigDecimal.ZERO) > 0) {
            usedPercent = dailyVolume.divide(agency.getDailyLimit(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        }

        long paidCount = monthlyTransfers.stream()
                .filter(t -> TransferStatus.PAID == t.getStatus()).count();
        BigDecimal successRate = monthlyTransfers.isEmpty() ? BigDecimal.ZERO
                : BigDecimal.valueOf(paidCount)
                .divide(BigDecimal.valueOf(monthlyTransfers.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);

        return AgencyPerformanceResponseDTO.builder()
                .agencyId(agency.getId()).name(agency.getName())
                .agencyName(agency.getName()).country(agency.getCountry())
                .dailyVolume(dailyVolume).dailyTransferCount(dailyTransfers.size())
                .dailyRevenue(dailyFees).monthlyVolume(monthlyVolume)
                .monthlyTransferCount(monthlyTransfers.size()).monthlyRevenue(monthlyFees)
                .operationCount(monthlyTransfers.size())
                .currentBalance(agency.getCurrentBalance() != null ? agency.getCurrentBalance() : BigDecimal.ZERO)
                .dailyLimit(agency.getDailyLimit() != null ? agency.getDailyLimit() : BigDecimal.ZERO)
                .dailyLimitUsedPercent(usedPercent).successRate(successRate)
                .reportDate(LocalDate.now()).build();
    }

    // ─── Balance ──────────────────────────────────────────────

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void checkAndDeductBalance(Long agencyId, BigDecimal amount) {
        Agency agency = findOrThrow(agencyId);
        BigDecimal currentBalance = agency.getCurrentBalance();
        if (currentBalance == null || currentBalance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    "Solde insuffisant dans l'agence '" + agency.getName()
                    + "'. Disponible: " + currentBalance + ", Requis: " + amount);
        }
        agency.setCurrentBalance(currentBalance.subtract(amount));
        agencyRepository.save(agency);
    }

    @Transactional
    @Override
    public void addBalance(Long agencyId, BigDecimal amount) {
        Agency agency = findOrThrow(agencyId);
        agency.setCurrentBalance(agency.getCurrentBalance().add(amount));
        agencyRepository.save(agency);
    }

    // ─── Helpers ───────────────────────────────────────────────

    private Agency findOrThrow(Long id) {
        return agencyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agency not found with id: " + id));
    }

    private AgencyResponseDTO toDTO(Agency agency) {
        int agentCount = agentRepository.findByAgency_Id(agency.getId()).size();
        AgencyResponseDTO dto = new AgencyResponseDTO();
        dto.setId(agency.getId());
        dto.setName(agency.getName());
        dto.setAddress(agency.getAddress());
        dto.setCountry(agency.getCountry());
        dto.setAgentCount(agentCount);
        dto.setDailyLimit(agency.getDailyLimit());
        dto.setCurrentBalance(agency.getCurrentBalance());
        dto.setActive(agency.isActive());
        return dto;
    }
}
