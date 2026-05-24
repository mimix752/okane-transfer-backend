package com.okanetransfer.service.impl;

import com.okanetransfer.dto.request.AgencyRequestDTO;
import com.okanetransfer.dto.response.AgencyPerformanceResponseDTO;
import com.okanetransfer.dto.response.AgencyResponseDTO;
import com.okanetransfer.entity.Agency;
import com.okanetransfer.entity.Agent;
import com.okanetransfer.entity.User;
import com.okanetransfer.enums.Role;
import com.okanetransfer.repository.AgencyRepository;
import com.okanetransfer.repository.AgentRepository;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.AgencyService;
import com.okanetransfer.service.AuditService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgencyServiceImpl implements AgencyService {

    @Autowired
    private AgencyRepository agencyRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    @Transactional(readOnly = true)
    @Override
    public List<AgencyResponseDTO> getAllAgencies(String country, Boolean active) {
        List<Agency> agencies;

        if (country != null && active != null) {
            agencies = agencyRepository.findByCountry(country)
                    .stream()
                    .filter(a -> a.isActive() == active)
                    .collect(Collectors.toList());
        } else if (country != null) {
            agencies = agencyRepository.findByCountry(country);
        } else if (active != null) {
            agencies = agencyRepository.findByActive(active);
        } else {
            agencies = agencyRepository.findAll();
        }

        return agencies.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public AgencyResponseDTO getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Transactional
    @Override
    public AgencyResponseDTO create(AgencyRequestDTO dto, String adminIp) {
        Agency agency = new Agency();
        agency.setName(dto.getName());
        agency.setAddress(dto.getAddress());
        agency.setCountry(dto.getCountry());
        agency.setDestinationCountry(dto.getDestinationCountry());
        agency.setDailyLimit(dto.getDailyLimit());
        agency.setCurrentBalance(BigDecimal.ZERO);
        agency.setActive(true);

        Agency saved = agencyRepository.save(agency);

        auditService.logAction(
                adminIp, "CREATE_AGENCY",
                "Agency", saved.getId(),
                null, saved.getName(),
                adminIp
        );

        return toDTO(saved);
    }

    @Transactional
    @Override
    public AgencyResponseDTO update(Long id, AgencyRequestDTO dto, String adminIp) {
        Agency agency = findOrThrow(id);
        String oldName = agency.getName();

        agency.setName(dto.getName());
        agency.setAddress(dto.getAddress());
        agency.setCountry(dto.getCountry());
        agency.setDestinationCountry(dto.getDestinationCountry());
        agency.setDailyLimit(dto.getDailyLimit());

        Agency saved = agencyRepository.save(agency);

        auditService.logAction(
                adminIp, "UPDATE_AGENCY",
                "Agency", id,
                oldName, saved.getName(),
                adminIp
        );

        return toDTO(saved);
    }

    @Transactional
    @Override
    public void toggle(Long id, String adminIp) {
        Agency agency = findOrThrow(id);
        boolean previousState = agency.isActive();
        agency.setActive(!previousState);
        agencyRepository.save(agency);

        auditService.logAction(
                adminIp,
                previousState ? "DISABLE_AGENCY" : "ENABLE_AGENCY",
                "Agency", id,
                String.valueOf(previousState),
                String.valueOf(!previousState),
                adminIp
        );
    }

    @Transactional
    @Override
    public void addAgent(Long agencyId, Long userId, String adminIp) {
        Agency agency = findOrThrow(agencyId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Agent agent = new Agent();
        agent.setAgency(agency);
        agent.setActive(true);
        agent.setId(user.getId());
        agent.setUsername(user.getUsername());
        agent.setEmail(user.getEmail());
        agent.setPassword(user.getPassword());
        agent.setPhone(user.getPhone());
        agent.setRole(Role.AGENT);
        agent.setEnabled(user.isEnabled());

        agentRepository.save(agent);

        auditService.logAction(
                adminIp, "ADD_AGENT",
                "Agency", agencyId,
                null, "userId=" + userId,
                adminIp
        );
    }

    @Transactional
    @Override
    public void removeAgent(Long agencyId, Long userId, String adminIp) {
        Agent agent = agentRepository.findByAgency_Id(agencyId)
                .stream()
                .filter(a -> a.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Agent not found for agencyId=" + agencyId + " userId=" + userId));

        agent.setActive(false);
        agentRepository.save(agent);

        auditService.logAction(
                adminIp, "REMOVE_AGENT",
                "Agency", agencyId,
                "userId=" + userId, null,
                adminIp
        );
    }

    @Transactional(readOnly = true)
    @Override
    public AgencyPerformanceResponseDTO getPerformance(Long id) {
        Agency agency = findOrThrow(id);
        int agentCount = agentRepository.findByAgency_Id(id).size();

        AgencyPerformanceResponseDTO dto = new AgencyPerformanceResponseDTO();
        dto.setAgencyId(agency.getId());
        dto.setName(agency.getName());
        dto.setDailyVolume(BigDecimal.ZERO);
        dto.setMonthlyVolume(BigDecimal.ZERO);
        dto.setOperationCount(agentCount);
        dto.setDailyRevenue(BigDecimal.ZERO);
        dto.setMonthlyRevenue(BigDecimal.ZERO);

        return dto;
    }

    // ─── helpers ───────────────────────────────────────────────

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
        dto.setActive(agency.isActive());

        return dto;
    }
}