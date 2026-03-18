package com.ecommerce.order_service.service;

import com.ecommerce.order_service.model.*;
import com.ecommerce.order_service.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ecommerce.order_service.dto.PaymentRequestDTO;
import com.ecommerce.order_service.dto.PaymentResponseDTO;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final RestTemplate restTemplate;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        CartItemRepository cartItemRepository,
                        OrderItemRepository orderItemRepository,
                        RestTemplate restTemplate) {

        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.restTemplate = restTemplate;
    }

    // 🔥 CHECKOUT WITH JWT PROPAGATION
    public Order checkout(String username, HttpServletRequest httpRequest){

        String authHeader = httpRequest.getHeader("Authorization");

        Cart cart = cartRepository.findByUserId(username)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        if(cartItems.isEmpty()){
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setUserId(username);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        double total = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        // 🔥 COMMON HEADERS FOR ALL CALLS
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);

        for(CartItem cartItem : cartItems){

            // ---------- CHECK INVENTORY ----------
            String checkUrl =
                    "http://localhost:8082/inventory/check/" +
                            cartItem.getProductId() +
                            "?quantity=" + cartItem.getQuantity();

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Boolean> inventoryResponse =
                    restTemplate.exchange(
                            checkUrl,
                            HttpMethod.GET,
                            entity,
                            Boolean.class
                    );

            Boolean available = inventoryResponse.getBody();

            if(Boolean.FALSE.equals(available)){
                throw new RuntimeException(
                        "Not enough stock for product " + cartItem.getProductId()
                );
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setOrder(order);

            total += cartItem.getPrice() * cartItem.getQuantity();
            orderItems.add(orderItem);
        }

        order.setTotalAmount(total);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // ---------- CALL PAYMENT SERVICE ----------
        String paymentUrl = "http://localhost:8083/payments/initiate";

        PaymentRequestDTO paymentRequest = new PaymentRequestDTO();
        paymentRequest.setOrderId(savedOrder.getOrderId());
        paymentRequest.setAmount(savedOrder.getTotalAmount());
        paymentRequest.setPaymentMethod("UPI");

        HttpEntity<PaymentRequestDTO> paymentEntity =
                new HttpEntity<>(paymentRequest, headers);

        ResponseEntity<PaymentResponseDTO> paymentResponseEntity =
                restTemplate.exchange(
                        paymentUrl,
                        HttpMethod.POST,
                        paymentEntity,
                        PaymentResponseDTO.class
                );

        PaymentResponseDTO response = paymentResponseEntity.getBody();

        if(response != null && "SUCCESS".equals(response.getStatus())){

            savedOrder.setStatus(OrderStatus.CONFIRMED);

            // ---------- REDUCE INVENTORY ----------
            for(CartItem cartItem : cartItems){

                String reduceUrl =
                        "http://localhost:8082/inventory/reduce/" +
                                cartItem.getProductId() +
                                "?quantity=" + cartItem.getQuantity();

                HttpEntity<Void> reduceEntity = new HttpEntity<>(headers);

                restTemplate.exchange(
                        reduceUrl,
                        HttpMethod.PUT,
                        reduceEntity,
                        Void.class
                );
            }

        }else{
            savedOrder.setStatus(OrderStatus.CANCELLED);
        }

        orderRepository.save(savedOrder);

        // ---------- CLEAR CART ----------
        cartItemRepository.deleteAll(cartItems);

        return savedOrder;
    }

    public Order createOrder(String username, Order order){
        order.setUserId(username);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        return orderRepository.save(order);
    }

    public List<Order> getOrders(String username){
        return orderRepository.findByUserId(username);
    }

    public Order cancelOrder(Long orderId){

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.CANCELLED);

        return orderRepository.save(order);
    }
}