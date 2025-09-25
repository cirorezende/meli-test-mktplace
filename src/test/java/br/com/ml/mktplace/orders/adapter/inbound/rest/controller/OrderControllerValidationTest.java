package br.com.ml.mktplace.orders.adapter.inbound.rest.controller;

import br.com.ml.mktplace.orders.adapter.inbound.rest.mapper.OrderRestMapper;
import br.com.ml.mktplace.orders.domain.port.CreateOrderUseCase;
import br.com.ml.mktplace.orders.domain.port.QueryOrderUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
class OrderControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateOrderUseCase createOrderUseCase;

    @MockBean
    private QueryOrderUseCase queryOrderUseCase;

    @MockBean
    private OrderRestMapper mapper;

    @Test
    @DisplayName("Should reject order creation when coordinates are provided in deliveryAddress")
    void shouldRejectCoordinatesOnCreate() throws Exception {
        String payload = "{\n" +
                "  \"customerId\": \"CUST-XYZ\",\n" +
                "  \"items\": [ { \"itemId\": \"IT-1\", \"quantity\": 1 } ],\n" +
                "  \"deliveryAddress\": {\n" +
                "    \"street\": \"Rua A\",\n" +
                "    \"number\": \"10\",\n" +
                "    \"city\": \"Cidade\",\n" +
                "    \"state\": \"ST\",\n" +
                "    \"country\": \"BR\",\n" +
                "    \"zipCode\": \"12345-678\",\n" +
                "    \"coordinates\": { \"latitude\": -10.0, \"longitude\": -50.0 }\n" +
                "  }\n" +
                "}";

        mockMvc.perform(post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        // ensure mapper/create use case were never invoked
        Mockito.verifyNoInteractions(mapper, createOrderUseCase);
    }
}
