package com.govind.ecommerce.repo;

import com.govind.ecommerce.model.Cart;
import com.govind.ecommerce.model.CartItem;
import com.govind.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}