package com.govind.ecommerce.repo;

import com.govind.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
            SELECT p FROM Product p
            WHERE (:keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:category = '' OR LOWER(p.category) = LOWER(:category))
            """)
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("category") String category,
            Pageable pageable
    );
}