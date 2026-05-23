package com.okanetransfer.controller;

import com.okanetransfer.dto.request.CorridorRequestDTO;
import com.okanetransfer.dto.request.CurrencyRequestDTO;
import com.okanetransfer.dto.response.CorridorResponseDTO;
import com.okanetransfer.dto.response.CurrencyResponseDTO;
import com.okanetransfer.service.CorridorService;
import com.okanetransfer.service.CurrencyService;
import com.okanetransfer.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(
        name = "Currency & Corridor Management",
        description = "Admin endpoints for managing currencies and corridors"
)
@SecurityRequirement(name = "Bearer Authentication")
public class CurrencyController {

    private final CurrencyService  currencyService;
    private final CorridorService  corridorService;
    @Operation(
            summary     = "Get all currencies",
            description = "Returns all currencies. "
                    + "Use ?activeOnly=true to get only active ones."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description  = "List of currencies returned"
    )
    @GetMapping("/currencies")
    public ResponseEntity<ApiResponse<List<CurrencyResponseDTO>>>
    getAllCurrencies(
            @Parameter(description = "Filter active only")
            @RequestParam(defaultValue = "false")
            boolean activeOnly) {

        List<CurrencyResponseDTO> result = activeOnly
                ? currencyService.getActiveCurrencies()
                : currencyService.getAllCurrencies();

        return ResponseEntity.ok(
                ApiResponse.success("Currencies retrieved", result)
        );
    }

    @Operation(
            summary     = "Get currency by ID",
            description = "Returns a single currency by its ID"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Currency found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Currency not found")
    })
    @GetMapping("/currencies/{id}")
    public ResponseEntity<ApiResponse<CurrencyResponseDTO>>
    getCurrencyById(
            @Parameter(description = "Currency ID")
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(currencyService.getById(id))
        );
    }

    @Operation(
            summary     = "Get currency by code",
            description = "Returns a currency by its ISO code. Ex: EUR"
    )
    @GetMapping("/currencies/code/{code}")
    public ResponseEntity<ApiResponse<CurrencyResponseDTO>>
    getCurrencyByCode(
            @Parameter(description = "ISO 4217 code. Ex: EUR")
            @PathVariable String code) {

        return ResponseEntity.ok(
                ApiResponse.success(currencyService.getByCode(code))
        );
    }

    @Operation(
            summary     = "Create a new currency",
            description = "Creates a new currency. Code must be unique "
                    + "and exactly 3 uppercase letters."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "Currency created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "Code already exists")
    })
    @PostMapping("/currencies")
    public ResponseEntity<ApiResponse<CurrencyResponseDTO>>
    createCurrency(
            @Valid @RequestBody CurrencyRequestDTO dto,
            HttpServletRequest request) {

        CurrencyResponseDTO created = currencyService.create(
                dto, request.getRemoteAddr());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Currency created successfully", created));
    }

    @Operation(
            summary     = "Update a currency",
            description = "Updates an existing currency by ID"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Currency updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Currency not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Validation error")
    })
    @PutMapping("/currencies/{id}")
    public ResponseEntity<ApiResponse<CurrencyResponseDTO>>
    updateCurrency(
            @Parameter(description = "Currency ID")
            @PathVariable Long id,
            @Valid @RequestBody CurrencyRequestDTO dto,
            HttpServletRequest request) {

        CurrencyResponseDTO updated = currencyService.update(
                id, dto, request.getRemoteAddr());

        return ResponseEntity.ok(
                ApiResponse.success("Currency updated successfully",
                        updated)
        );
    }

    @Operation(
            summary     = "Toggle currency active status",
            description = "Activates or deactivates a currency. "
                    + "Cannot deactivate if used by active corridors."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Status toggled"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description  = "Currency used by active corridors")
    })
    @PatchMapping("/currencies/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleCurrency(
            @Parameter(description = "Currency ID")
            @PathVariable Long id,
            HttpServletRequest request) {

        currencyService.toggle(id, request.getRemoteAddr());

        return ResponseEntity.ok(
                ApiResponse.success("Currency status toggled", null)
        );
    }

    @Operation(
            summary     = "Get all corridors",
            description = "Returns all corridors. "
                    + "Use ?activeOnly=true to get only active ones. "
                    + "Use ?source=MA to filter by source country."
    )
    @GetMapping("/corridors")
    public ResponseEntity<ApiResponse<List<CorridorResponseDTO>>>
    getAllCorridors(
            @Parameter(description = "Filter active only")
            @RequestParam(defaultValue = "false")
            boolean activeOnly,

            @Parameter(description = "Filter by source country. Ex: MA")
            @RequestParam(required = false)
            String source) {

        List<CorridorResponseDTO> result;

        if (source != null && !source.isBlank()) {
            // Filtrer par pays source
            result = corridorService.getBySourceCountry(source);
        } else if (activeOnly) {
            result = corridorService.getActiveCorridors();
        } else {
            result = corridorService.getAllCorridors();
        }

        return ResponseEntity.ok(
                ApiResponse.success("Corridors retrieved", result)
        );
    }

    @Operation(
            summary     = "Get corridor by ID",
            description = "Returns a single corridor by its ID"
    )
    @GetMapping("/corridors/{id}")
    public ResponseEntity<ApiResponse<CorridorResponseDTO>>
    getCorridorById(
            @Parameter(description = "Corridor ID")
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(corridorService.getById(id))
        );
    }

    @Operation(
            summary     = "Create a new corridor",
            description = "Creates a transfer corridor between two countries. "
                    + "The source/destination pair must be unique."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "Corridor created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description  = "Corridor already exists")
    })
    @PostMapping("/corridors")
    public ResponseEntity<ApiResponse<CorridorResponseDTO>>
    createCorridor(
            @Valid @RequestBody CorridorRequestDTO dto,
            HttpServletRequest request) {

        CorridorResponseDTO created = corridorService.create(
                dto, request.getRemoteAddr());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Corridor created successfully", created));
    }

    @Operation(
            summary     = "Update a corridor",
            description = "Updates an existing corridor by ID"
    )
    @PutMapping("/corridors/{id}")
    public ResponseEntity<ApiResponse<CorridorResponseDTO>>
    updateCorridor(
            @Parameter(description = "Corridor ID")
            @PathVariable Long id,
            @Valid @RequestBody CorridorRequestDTO dto,
            HttpServletRequest request) {

        CorridorResponseDTO updated = corridorService.update(
                id, dto, request.getRemoteAddr());

        return ResponseEntity.ok(
                ApiResponse.success("Corridor updated successfully",
                        updated)
        );
    }

    @Operation(
            summary     = "Toggle corridor active status",
            description = "Activates or deactivates a corridor"
    )
    @PatchMapping("/corridors/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleCorridor(
            @Parameter(description = "Corridor ID")
            @PathVariable Long id,
            HttpServletRequest request) {

        corridorService.toggle(id, request.getRemoteAddr());

        return ResponseEntity.ok(
                ApiResponse.success("Corridor status toggled", null)
        );
    }
}