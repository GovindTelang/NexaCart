package com.govind.ecommerce.repo;

import com.govind.ecommerce.model.Product;
import com.govind.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product,Long> {
}
