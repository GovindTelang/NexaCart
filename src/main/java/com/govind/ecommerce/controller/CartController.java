package com.govind.ecommerce.controller;

import com.govind.ecommerce.model.Cart;
import com.govind.ecommerce.service.CartService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@CrossOrigin("*")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
    }

    @GetMapping
    public Cart getCart() {
        return cartService.getOrCreateCart(getCurrentUserEmail());
    }

    @PostMapping("/add")
    public Cart addToCart(
            @RequestParam Long productId,
            @RequestParam int quantity) {

        return cartService.addToCart(
                getCurrentUserEmail(),
                productId,
                quantity
        );
    }

    @PutMapping("/update")
    public Cart updateQuantity(
            @RequestParam Long productId,
            @RequestParam int quantity) {

        return cartService.updateQuantity(
                getCurrentUserEmail(),
                productId,
                quantity
        );
    }

    @DeleteMapping("/remove")
    public void removeFromCart(@RequestParam Long productId) {

        cartService.removeFromCart(
                getCurrentUserEmail(),
                productId
        );
    }
}