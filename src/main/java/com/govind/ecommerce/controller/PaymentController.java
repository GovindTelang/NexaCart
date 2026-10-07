package com.govind.ecommerce.controller;

import com.govind.ecommerce.service.PaymentService;
import com.govind.ecommerce.service.EmailService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.govind.ecommerce.dto.OrderDTO;
import com.govind.ecommerce.service.OrderService;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@CrossOrigin("*")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final EmailService emailService;

    public PaymentController(
            PaymentService paymentService,
            OrderService orderService,
            EmailService emailService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
        this.emailService = emailService;
    }

    @PostMapping("/create-order")
    public Map<String, Object> createOrder() throws Exception {

        String email =
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName();

        return paymentService.createOrder(email);
    }

    @PostMapping("/verify")
    public OrderDTO verifyPayment(
            @RequestParam String razorpayOrderId,
            @RequestParam String razorpayPaymentId,
            @RequestParam String razorpaySignature) throws Exception {

        boolean verified = paymentService.verifyPayment(
                razorpayOrderId,
                razorpayPaymentId,
                razorpaySignature
        );

        if (!verified) {
            throw new IllegalArgumentException("Payment verification failed");
        }

        String email =
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName();

        OrderDTO order = orderService.checkout(email);
        emailService.sendOrderConfirmation(email);
        return order;
    }
}