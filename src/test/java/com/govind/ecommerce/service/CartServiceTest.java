package com.govind.ecommerce.service;

import com.govind.ecommerce.model.Cart;
import com.govind.ecommerce.model.CartItem;
import com.govind.ecommerce.model.Product;
import com.govind.ecommerce.model.User;
import com.govind.ecommerce.repo.CartItemRepository;
import com.govind.ecommerce.repo.CartRepository;
import com.govind.ecommerce.repo.ProductRepository;
import com.govind.ecommerce.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    private CartService cartService;

    private User user;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {

        cartService = new CartService(
                cartRepository,
                cartItemRepository,
                productRepository,
                userRepository
        );

        user = new User();
        user.setName("Test User");
        user.setEmail("test@nexacart.local");
        user.setPassword("hashed-password");

        product = new Product();
        product.setName("iPhone");
        product.setPrice(134999);
        product.setCategory("Electronics");
        product.setStockQuantity(10);

        cart = new Cart();
        cart.setUser(user);
    }

    @Test
    void addToCart_shouldAddNewProduct() {

        when(userRepository.findByEmail("test@nexacart.local"))
                .thenReturn(user);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartAndProduct(cart, product))
                .thenReturn(Optional.empty());

        when(cartItemRepository.save(any(CartItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.addToCart(
                "test@nexacart.local",
                1L,
                2
        );

        assertNotNull(result);

        verify(cartItemRepository).save(argThat(item ->
                item.getProduct() == product &&
                item.getCart() == cart &&
                item.getQuantity() == 2
        ));
    }

    @Test
    void addToCart_shouldAccumulateQuantityForExistingProduct() {

        CartItem existingItem = new CartItem();
        existingItem.setCart(cart);
        existingItem.setProduct(product);
        existingItem.setQuantity(2);

        when(userRepository.findByEmail("test@nexacart.local"))
                .thenReturn(user);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartAndProduct(cart, product))
                .thenReturn(Optional.of(existingItem));

        when(cartItemRepository.save(existingItem))
                .thenReturn(existingItem);

        cartService.addToCart(
                "test@nexacart.local",
                1L,
                3
        );

        assertEquals(5, existingItem.getQuantity());

        verify(cartItemRepository).save(existingItem);
    }

    @Test
    void addToCart_shouldRejectNonPositiveQuantity() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addToCart(
                        "test@nexacart.local",
                        1L,
                        0
                )
        );

        assertEquals(
                "Quantity must be greater than zero",
                exception.getMessage()
        );

        verifyNoInteractions(
                userRepository,
                productRepository,
                cartRepository,
                cartItemRepository
        );
    }

    @Test
    void addToCart_shouldRejectQuantityGreaterThanStock() {

        when(userRepository.findByEmail("test@nexacart.local"))
                .thenReturn(user);

        product.setStockQuantity(5);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cartService.addToCart(
                        "test@nexacart.local",
                        1L,
                        10
                )
        );

        assertEquals("Insufficient stock", exception.getMessage());

        verify(cartItemRepository, never())
                .save(any(CartItem.class));
    }

    @Test
    void updateQuantity_shouldUpdateExistingCartItem() {

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        when(userRepository.findByEmail("test@nexacart.local"))
                .thenReturn(user);

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(cartItemRepository.findByCartAndProduct(cart, product))
                .thenReturn(Optional.of(cartItem));

        when(cartItemRepository.save(cartItem))
                .thenReturn(cartItem);

        Cart result = cartService.updateQuantity(
                "test@nexacart.local",
                1L,
                5
        );

        assertNotNull(result);
        assertEquals(5, cartItem.getQuantity());

        verify(cartItemRepository).save(cartItem);
    }
}