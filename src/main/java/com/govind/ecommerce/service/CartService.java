package com.govind.ecommerce.service;

import com.govind.ecommerce.model.Cart;
import com.govind.ecommerce.model.CartItem;
import com.govind.ecommerce.model.Product;
import com.govind.ecommerce.model.User;
import com.govind.ecommerce.repo.CartItemRepository;
import com.govind.ecommerce.repo.CartRepository;
import com.govind.ecommerce.repo.ProductRepository;
import com.govind.ecommerce.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public Cart getOrCreateCart(String email) {

        User user = getUser(email);

        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    @Transactional
    public Cart addToCart(String email, Long productId, int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        User user = getUser(email);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (quantity > product.getStockQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }

        Cart cart = getOrCreateCart(email);

        CartItem cartItem = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElse(null);

        if (cartItem == null) {

            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);

        } else {

            int newQuantity = cartItem.getQuantity() + quantity;

            if (newQuantity > product.getStockQuantity()) {
                throw new RuntimeException("Insufficient stock");
            }

            cartItem.setQuantity(newQuantity);
        }

        cartItemRepository.save(cartItem);

        return cart;
    }

    @Transactional
    public Cart updateQuantity(
            String email,
            Long productId,
            int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Cart cart = getOrCreateCart(email);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (quantity > product.getStockQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }

        CartItem cartItem = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElseThrow(() ->
                        new RuntimeException("Product not found in cart"));

        cartItem.setQuantity(quantity);

        cartItemRepository.save(cartItem);

        return cart;
    }

    @Transactional
    public void removeFromCart(String email, Long productId) {

        Cart cart = getOrCreateCart(email);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem cartItem = cartItemRepository
                .findByCartAndProduct(cart, product)
                .orElseThrow(() ->
                        new RuntimeException("Product not found in cart"));

        cartItemRepository.delete(cartItem);
    }
}