package com.govind.ecommerce.security;

import com.govind.ecommerce.config.SecurityConfig;
import com.govind.ecommerce.controller.OrderController;
import com.govind.ecommerce.controller.ProductController;
import com.govind.ecommerce.model.Role;
import com.govind.ecommerce.service.OrderService;
import com.govind.ecommerce.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        ProductController.class,
        OrderController.class
})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class
})
@TestPropertySource(properties =
        "jwt.secret=nexacart-test-secret-key-2026-this-is-long-enough"
)
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private ProductService productService;

    @MockBean
    private OrderService orderService;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {

        userToken = jwtService.generateToken(
                "user@nexacart.local",
                Role.USER.name()
        );

        adminToken = jwtService.generateToken(
                "admin@nexacart.local",
                Role.ADMIN.name()
        );
    }

    @Test
    void userShouldBeAbleToGetProducts() throws Exception {

        when(productService.getAllProducts(any()))
                .thenReturn(Page.empty());

        mockMvc.perform(
                get("/products")
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
        )
        .andExpect(status().isOk());

        verify(productService).getAllProducts(any());
    }

    @Test
    void userShouldBeAbleToGetOwnOrders() throws Exception {

        when(orderService.getOrdersByUser("user@nexacart.local"))
                .thenReturn(java.util.List.of());

        mockMvc.perform(
                get("/orders/my-orders")
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
        )
        .andExpect(status().isOk());

        verify(orderService)
                .getOrdersByUser("user@nexacart.local");
    }

    @Test
    void userShouldNotBeAbleToGetAllOrders() throws Exception {

        mockMvc.perform(
                get("/orders/all-orders")
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
        )
        .andExpect(status().isForbidden());

        verify(orderService, never()).getAllOrders();
    }

    @Test
    void adminShouldBeAbleToGetAllOrders() throws Exception {

        when(orderService.getAllOrders())
                .thenReturn(java.util.List.of());

        mockMvc.perform(
                get("/orders/all-orders")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
        )
        .andExpect(status().isOk());

        verify(orderService).getAllOrders();
    }

    @Test
    void requestWithoutTokenShouldBeAllowedForProducts() throws Exception {

        when(productService.getAllProducts(any()))
                .thenReturn(Page.empty());

        mockMvc.perform(
                get("/products")
        )
        .andExpect(status().isOk());

        verify(productService).getAllProducts(any());
    }
}