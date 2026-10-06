package com.govind.ecommerce.controller;

import com.govind.ecommerce.dto.OrderDTO;
import com.govind.ecommerce.service.OrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@CrossOrigin("*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public OrderDTO checkout() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return orderService.checkout(email);
    }

    @GetMapping("/all-orders")
    public List<OrderDTO> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/my-orders")
    public List<OrderDTO> getMyOrders() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return orderService.getOrdersByUser(email);
    }
}