package com.govind.ecommerce.repo;

import com.govind.ecommerce.model.Cart;
import com.govind.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);
}