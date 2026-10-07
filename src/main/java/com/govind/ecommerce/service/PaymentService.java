package com.govind.ecommerce.service;

import com.govind.ecommerce.model.Cart;
import com.govind.ecommerce.model.CartItem;
import com.govind.ecommerce.model.Product;
import com.govind.ecommerce.model.User;
import com.govind.ecommerce.repo.CartRepository;
import com.govind.ecommerce.repo.UserRepository;
import com.razorpay.Order;
import com.razorpay.Utils;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PaymentService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final RazorpayClient razorpayClient;
    private final String razorpayKeyId;
    private final String razorpayKeySecret;

    public PaymentService(
            CartRepository cartRepository,
            UserRepository userRepository,
            @Value("${razorpay.key.id}") String razorpayKeyId,
            @Value("${razorpay.key.secret}") String razorpayKeySecret) throws RazorpayException {

        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.razorpayKeyId = razorpayKeyId;
        this.razorpayKeySecret = razorpayKeySecret;
        this.razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> createOrder(String email) throws RazorpayException {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        double totalAmount = 0.0;

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();
            int quantity = cartItem.getQuantity();

            if (quantity <= 0) {
                throw new IllegalArgumentException("Invalid quantity");
            }

            if (quantity > product.getStockQuantity()) {
                throw new RuntimeException(
                        "Insufficient stock for " + product.getName()
                );
            }

            totalAmount += product.getPrice() * quantity;
        }

        long amountInPaise = Math.round(totalAmount * 100);

        JSONObject options = new JSONObject();
        options.put("amount", amountInPaise);
        options.put("currency", "INR");
        options.put(
                "receipt",
                "NEXA-" + System.currentTimeMillis()
        );

        JSONObject notes = new JSONObject();
        notes.put("customer_email", email);
        notes.put("platform", "NexaCart");

        options.put("notes", notes);

        Order razorpayOrder = razorpayClient.orders.create(options);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("keyId", razorpayKeyId);
        response.put("orderId", razorpayOrder.get("id"));
        response.put("amount", amountInPaise);
        response.put("currency", "INR");
        response.put("customerEmail", email);

        return response;
    }

    public boolean verifyPayment(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature) throws RazorpayException {

        JSONObject attributes = new JSONObject();
        attributes.put("razorpay_order_id", razorpayOrderId);
        attributes.put("razorpay_payment_id", razorpayPaymentId);
        attributes.put("razorpay_signature", razorpaySignature);

        return Utils.verifyPaymentSignature(attributes, razorpayKeySecret);
    }
}