package com.okanetransfer.controller;

import com.okanetransfer.dto.response.FeeGridResponseDTO;
import com.okanetransfer.service.FeeGridService;
import com.okanetransfer.util.ApiResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class FeeGridControllerSimulateTest {

    @Mock
    private FeeGridService feeGridService;

    @InjectMocks
    private FeeGridController feeGridController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(feeGridController).build();
    }

    @Test
    public void testSimulateEndpointWithGetMethod() throws Exception {
        FeeGridResponseDTO mockResponse = new FeeGridResponseDTO();
        mockResponse.setId(1L);
        mockResponse.setMinAmount(BigDecimal.ZERO);
        mockResponse.setMaxAmount(new BigDecimal("10000"));
        mockResponse.setFixedFee(new BigDecimal("500"));
        mockResponse.setPercentageFee(new BigDecimal("2.5"));
        mockResponse.setSimulatedFeeForMaxAmount(new BigDecimal("625"));

        when(feeGridService.simulate(1L, new BigDecimal("5000")))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/admin/fee-grids/simulate")
                .param("corridorId", "1")
                .param("amount", "5000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.simulatedFeeForMaxAmount").value(625));
    }

    @Test
    public void testSimulateReturnsCorrectFeeCalculation() throws Exception {
        FeeGridResponseDTO mockResponse = new FeeGridResponseDTO();
        mockResponse.setId(2L);
        mockResponse.setFixedFee(new BigDecimal("1000"));
        mockResponse.setPercentageFee(new BigDecimal("1.5"));
        mockResponse.setSimulatedFeeForMaxAmount(new BigDecimal("1750"));

        when(feeGridService.simulate(2L, new BigDecimal("50000")))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/admin/fee-grids/simulate")
                .param("corridorId", "2")
                .param("amount", "50000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fixedFee").value(1000))
                .andExpect(jsonPath("$.data.percentageFee").value(1.5))
                .andExpect(jsonPath("$.data.simulatedFeeForMaxAmount").value(1750));
    }

    @Test
    public void testSimulateWithDirectResponseObject() {
        FeeGridResponseDTO mockResponse = new FeeGridResponseDTO();
        mockResponse.setId(1L);
        mockResponse.setSimulatedFeeForMaxAmount(new BigDecimal("625"));

        when(feeGridService.simulate(1L, new BigDecimal("5000")))
                .thenReturn(mockResponse);

        ResponseEntity<ApiResponse<FeeGridResponseDTO>> response =
                feeGridController.simulate(1L, new BigDecimal("5000"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Fee simulation completed", response.getBody().getMessage());
        assertEquals(new BigDecimal("625"), 
                response.getBody().getData().getSimulatedFeeForMaxAmount());
    }
}
