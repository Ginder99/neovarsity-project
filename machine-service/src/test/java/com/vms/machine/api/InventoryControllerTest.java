package com.vms.machine.api;

import com.vms.machine.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.vms.machine.dto.AddInventoryRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    @Test
    @WithMockUser(roles = "USER")
    void getInventory_ReturnsEmptyListWhenNoInventory() throws Exception {
        when(inventoryService.getAvailableInventory("1")).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/machines/1/inventory"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MACHINE_HANDLER")
    void addInventory_Success() throws Exception {
        AddInventoryRequest request = new AddInventoryRequest(1L, "A1", BigDecimal.TEN, 10);
        doNothing().when(inventoryService).addInventory(eq("1"), any(AddInventoryRequest.class));
        
        mockMvc.perform(post("/api/v1/machines/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\": 1, \"slotId\": \"A1\", \"price\": 10, \"quantity\": 10}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void addInventory_Forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/machines/1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\": 1, \"slotId\": \"A1\", \"price\": 10, \"quantity\": 10}"))
                .andExpect(status().isUnauthorized());
    }
}
