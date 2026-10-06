package com.govind.ecommerce.service;

import com.govind.ecommerce.model.Cart;
import com.govind.ecommerce.model.CartItem;
import com.govind.ecommerce.model.Orders;
import com.govind.ecommerce.model.Product;
import com.govind.ecommerce.model.User;
import com.govind.ecommerce.repo.CartRepository;
import com.govind.ecommerce.repo.OrderRepository;
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
class OrderServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    private OrderService orderService;

    private User user;
    private Product product;
    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {

        orderService = new OrderService();

        // Inject mocks into the private @Autowired fields
        // using reflection because OrderService currently
        // uses field injection.

        try {
            var cartRepoField =
                    OrderService.class.getDeclaredField("cartRepository");
            cartRepoField.setAccessible(true);
            cartRepoField.set(orderService, cartRepository);

            var userRepoField =
                    OrderService.class.getDeclaredField("userRepository");
            userRepoField.setAccessible(true);
            userRepoField.set(orderService, userRepository);

            var productRepoField =
                    OrderService.class.getDeclaredField("productRepository");
            productRepoField.setAccessible(true);
            productRepoField.set(orderService, productRepository);

            var orderRepoField =
                    OrderService.class.getDeclaredField("orderRepository");
            orderRepoField.setAccessible(true);
            orderRepoField.set(orderService, orderRepository);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        user = new User();
        user.setName("Test User");
        user.setEmail("test@nexacart.local");

        product = new Product();
        product.setName("iPhone");
        product.setPrice(134999);
        product.setCategory("Electronics");
        product.setStockQuantity(10);

        cart = new Cart();
        cart.setUser(user);

        cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        cart.getItems().add(cartItem);
    }

    @Test
    void checkout_shouldCreateOrderReduceStockAndClearCart() {

        when(userRepository.findByEmail("test@nexacart.local"))
                .thenReturn(user);

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(orderRepository.save(any(Orders.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(cartRepository.save(cart))
                .thenReturn(cart);

        var result = orderService.checkout(
                "test@nexacart.local"
        );

        assertNotNull(result);

        assertEquals(
                269998.0,
                result.getTotalAmount(),
                0.001
        );

        assertEquals("Pending", result.getStatus());

        assertEquals(
                8,
                product.getStockQuantity()
        );

        assertTrue(cart.getItems().isEmpty());

        verify(orderRepository).save(any(Orders.class));
        verify(cartRepository).save(cart);
    }

    @Test
    void checkout_shouldRejectWhenStockIsInsufficient() {

        product.setStockQuantity(1);

        when(userRepository.findByEmail("test@nexacart.local"))
                .thenReturn(user);

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.checkout(
                        "test@nexacart.local"
                )
        );

        assertEquals(
                "Insufficient stock for iPhone",
                exception.getMessage()
        );

        assertEquals(
                1,
                product.getStockQuantity()
        );

        assertFalse(cart.getItems().isEmpty());

        verify(orderRepository, never())
                .save(any(Orders.class));

        verify(cartRepository, never())
                .save(any(Cart.class));
    }
}