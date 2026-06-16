package com.okanetransfer.controller;

import com.okanetransfer.dto.response.AgencyPerformanceResponseDTO;
import com.okanetransfer.service.AgencyService;
import com.okanetransfer.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports/agencies")
@PreAuthorize("hasRole('ADMIN')")
@Tag(
        name        = "Agency Dashboard",
        description = "Performance metrics per agency"
)
@SecurityRequirement(name = "Bearer Authentication")
public class AgencyDashboardController {

    private final AgencyService agencyService;

    public AgencyDashboardController(AgencyService agencyService) {
        this.agencyService = agencyService;
    }

    @Operation(
            summary     = "Agency performance dashboard",
            description = "Returns performance metrics for a single agency: "
                    + "daily/monthly volume, CA, number of operations, "
                    + "balance vs daily limit."
    )
    @GetMapping("/{id}/performance")
    public ResponseEntity<ApiResponse<AgencyPerformanceResponseDTO>>
    getPerformance(
            @Parameter(description = "Agency ID")
            @PathVariable Long id) {

        AgencyPerformanceResponseDTO metrics =
                agencyService.getPerformance(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Performance metrics retrieved", metrics));
    }

    @Operation(
            summary     = "Dashboard for all active agencies",
            description = "Returns performance metrics for ALL active agencies. "
                    + "Sorted by monthly volume descending."
    )
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<
            List<AgencyPerformanceResponseDTO>>>
    getDashboard() {

        List<AgencyPerformanceResponseDTO> dashboard =
                agencyService.getAllPerformances();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Dashboard retrieved", dashboard));
    }
}