package com.vms.machine.api;

import com.vms.machine.dto.CreateMachineRequest;
import com.vms.machine.dto.MachineResponse;
import com.vms.machine.dto.NearbyMachinesResponse;
import com.vms.machine.service.MachineService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MachineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MachineService machineService;

    @Test
    @WithMockUser(roles = "MACHINE_HANDLER")
    void addMachine_Success() throws Exception {
        CreateMachineRequest request = new CreateMachineRequest(
                "Vending Machine 1",
                "123 Tech Street",
                37.7749,
                -122.4194
        );
        
        MachineResponse response = new MachineResponse(1L, "Vending Machine 1", "123 Tech Street", 37.7749, -122.4194, "ONLINE", null);
        when(machineService.addMachine(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/machines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Vending Machine 1")))
                .andExpect(jsonPath("$.status", is("ONLINE")));
    }

    @Test
    void addMachine_Unauthorized_WithoutRole() throws Exception {
        CreateMachineRequest request = new CreateMachineRequest(
                "Vending Machine 1",
                "123 Tech Street",
                37.7749,
                -122.4194
        );

        mockMvc.perform(post("/api/v1/machines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MACHINE_HANDLER")
    void findNearby_Success() throws Exception {
        NearbyMachinesResponse response = new NearbyMachinesResponse(List.of(), null, false);
        when(machineService.findNearbyMachines(anyDouble(), anyDouble(), any(), eq(null))).thenReturn(response);

        mockMvc.perform(get("/api/v1/machines/nearby")
                        .param("lat", "37.7749")
                        .param("lng", "-122.4194")
                        .param("radiusKm", "3.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.machines", is(org.hamcrest.Matchers.empty())));
    }
}
