package com.govind.ecommerce.service;

import com.govind.ecommerce.model.Product;
import com.govind.ecommerce.repo.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository);
    }

    @Test
    void getProductById_shouldReturnProductWhenProductExists() {

        Product product = new Product();
        product.setName("iPhone");
        product.setPrice(134999);
        product.setCategory("Electronics");
        product.setStockQuantity(25);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        Product result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("iPhone", result.getName());
        assertEquals(134999, result.getPrice());
        assertEquals(25, result.getStockQuantity());

        verify(productRepository).findById(1L);
    }

    @Test
    void updateProduct_shouldUpdateExistingProduct() {

        Product existingProduct = new Product();
        existingProduct.setName("Old Phone");
        existingProduct.setPrice(100000);
        existingProduct.setCategory("Electronics");
        existingProduct.setStockQuantity(10);

        Product updatedProduct = new Product();
        updatedProduct.setName("New Phone");
        updatedProduct.setDescription("Updated description");
        updatedProduct.setPrice(120000);
        updatedProduct.setImageUrl("https://example.com/phone.jpg");
        updatedProduct.setCategory("Smartphones");
        updatedProduct.setStockQuantity(20);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(existingProduct));

        when(productRepository.save(existingProduct))
                .thenReturn(existingProduct);

        Product result = productService.updateProduct(1L, updatedProduct);

        assertNotNull(result);
        assertEquals("New Phone", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(120000, result.getPrice());
        assertEquals("https://example.com/phone.jpg", result.getImageUrl());
        assertEquals("Smartphones", result.getCategory());
        assertEquals(20, result.getStockQuantity());

        verify(productRepository).findById(1L);
        verify(productRepository).save(existingProduct);
    }

    @Test
    void updateProduct_shouldReturnNullWhenProductDoesNotExist() {

        Product updatedProduct = new Product();
        updatedProduct.setName("New Phone");
        updatedProduct.setPrice(120000);
        updatedProduct.setCategory("Smartphones");
        updatedProduct.setStockQuantity(20);

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        Product result = productService.updateProduct(999L, updatedProduct);

        assertNull(result);

        verify(productRepository).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }
}