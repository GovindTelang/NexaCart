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

    @Query("""
            SELECT p FROM Product p
            ORDER BY
                CASE LOWER(p.category)
                    WHEN 'fashion' THEN 0
                    WHEN 'electronics' THEN 1
                    WHEN 'home & kitchen' THEN 2
                    WHEN 'books' THEN 3
                    WHEN 'beauty' THEN 4
                    WHEN 'office & study' THEN 5
                    WHEN 'gaming' THEN 6
                    WHEN 'sports' THEN 7
                    WHEN 'sports & fitness' THEN 7
                    WHEN 'grocery' THEN 8
                    ELSE 9
                END,
                p.id ASC
            """)
    Page<Product> findStorefrontProducts(Pageable pageable);
}