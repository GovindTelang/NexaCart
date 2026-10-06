package com.govind.ecommerce.service;

import com.govind.ecommerce.dto.OrderDTO;
import com.govind.ecommerce.dto.OrderItemDTO;
import com.govind.ecommerce.model.OrderItem;
import com.govind.ecommerce.model.Orders;
import com.govind.ecommerce.model.Product;
import com.govind.ecommerce.model.User;
import com.govind.ecommerce.repo.OrderRepository;
import com.govind.ecommerce.repo.ProductRepository;
import com.govind.ecommerce.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.govind.ecommerce.model.Cart;
import com.govind.ecommerce.model.CartItem;
import com.govind.ecommerce.repo.CartRepository;
import org.springframework.transaction.annotation.Transactional;
import java.net.Inet4Address;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class OrderService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;
    public OrderDTO placeOrder(Long userId, Map<Long, Integer> productQuantities, double totalAmount) {
       User user= userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("User not found"));

       Orders order=new Orders();
       order.setUser(user);
       order.setOrderDate(new Date());
       order.setStatus("Pending");
       order.setTotalAmount(totalAmount);

        List<OrderItem> orderItems=new ArrayList<>();
        List<OrderItemDTO> orderItemDTOS=new ArrayList<>();

        for(Map.Entry<Long, Integer> entry:productQuantities.entrySet())
        {
           Product product= productRepository.findById(entry.getKey())
                   .orElseThrow(()->new RuntimeException("Product Not found"));

           OrderItem orderItem=new OrderItem();
           orderItem.setOrder(order);
           orderItem.setProduct(product);
           orderItem.setQuantity(entry.getValue());
           orderItems.add(orderItem);

           orderItemDTOS.add(new OrderItemDTO(product.getName(),product.getPrice(),entry.getValue()));
        }

        order.setOrderItems(orderItems);
        Orders saveOrder = orderRepository.save(order);
        return new OrderDTO(saveOrder.getId(), saveOrder.getTotalAmount()
                ,saveOrder.getStatus(),saveOrder.getOrderDate(),orderItemDTOS);
    }

    public List<OrderDTO> getAllOrders() {
        List<Orders> orders = orderRepository.findAllOrdersWithUsers();
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private OrderDTO convertToDTO(Orders orders) {
        List<OrderItemDTO> OrderItems = orders.getOrderItems().stream()
                .map(item -> new OrderItemDTO(
                        item.getProduct().getName(),
                        item.getProduct().getPrice(),
                        item.getQuantity())).collect(Collectors.toList());
        return new OrderDTO(
                orders.getId(),
                orders.getTotalAmount(),
                orders.getStatus(),
                orders.getOrderDate(),
                orders.getUser()!=null ? orders.getUser().getName() : "Unknown",
                orders.getUser()!=null ? orders.getUser().getEmail() : "Unknown",
                OrderItems
        );
    }

    @Transactional
    public OrderDTO checkout(String email) {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Orders order = new Orders();
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setStatus("Pending");

        List<OrderItem> orderItems = new ArrayList<>();
        List<OrderItemDTO> orderItemDTOs = new ArrayList<>();

        double totalAmount = 0.0;

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();
            int quantity = cartItem.getQuantity();

            if (quantity <= 0) {
                throw new RuntimeException("Invalid quantity");
            }

            if (quantity > product.getStockQuantity()) {
                throw new RuntimeException(
                        "Insufficient stock for " + product.getName()
                );
            }

            totalAmount += product.getPrice() * quantity;

            product.setStockQuantity(
                    product.getStockQuantity() - quantity
            );

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);

            orderItems.add(orderItem);

            orderItemDTOs.add(
                    new OrderItemDTO(
                            product.getName(),
                            product.getPrice(),
                            quantity
                    )
            );
        }

        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);

        Orders savedOrder = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return new OrderDTO(
                savedOrder.getId(),
                savedOrder.getTotalAmount(),
                savedOrder.getStatus(),
                savedOrder.getOrderDate(),
                orderItemDTOs
        );
    }

    public List<OrderDTO> getOrdersByUser(String email) {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        List<Orders> ordersList = orderRepository.findByUser(user);

        return ordersList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
