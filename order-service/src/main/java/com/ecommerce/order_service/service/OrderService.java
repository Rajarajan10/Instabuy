package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.*;
import com.ecommerce.order_service.exception.*;
import com.ecommerce.order_service.mapper.OrderMapper;
import com.ecommerce.order_service.model.*;
import com.ecommerce.order_service.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final RestTemplate restTemplate;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        CartItemRepository cartItemRepository,
                        RestTemplate restTemplate,
                        OrderMapper orderMapper) {

        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.restTemplate = restTemplate;
        this.orderMapper = orderMapper;
    }

    public OrderResponseDTO checkout(String username, String authHeader){

        Cart cart = cartRepository.findByUserId(username)
                .orElseThrow(() -> new CartNotFoundException("Cart not found"));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        if(cartItems.isEmpty()){
            throw new EmptyCartException("Cart is empty");
        }

        Order order = new Order();
        order.setUserId(username);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        double total = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        HttpHeaders headers = new HttpHeaders();
        if (authHeader != null) {
            headers.set("Authorization", authHeader);
        }

        for(CartItem c : cartItems){

            //Quantity Check
            String checkUrl = "http://localhost:8082/inventory/check/"
                    + c.getProductId() + "?quantity=" + c.getQuantity();

            Boolean available;
            try {
                available = restTemplate.exchange(
                        checkUrl,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Boolean.class
                ).getBody();
            } catch (Exception e) {
                throw new InventoryServiceException("Inventory service unavailable");
            }

            // Insufficient Quantity
            if(Boolean.FALSE.equals(available)){
                throw new InsufficientStockException("Not enough stock for product " + c.getProductId());
            }

            OrderItem item = new OrderItem();
            item.setProductId(c.getProductId());
            item.setQuantity(c.getQuantity());
            item.setPrice(c.getPrice());
            item.setOrder(order);

            total += c.getPrice() * c.getQuantity();
            orderItems.add(item);
        }

        order.setItems(orderItems);
        order.setTotalAmount(total);

        Order savedOrder;
        try {
            savedOrder = orderRepository.save(order);
        } catch (Exception e) {
            throw new OrderCreationException("Failed to create order");
        }

        PaymentRequestDTO paymentRequest = new PaymentRequestDTO();
        paymentRequest.setOrderId(savedOrder.getOrderId());
        paymentRequest.setAmount(savedOrder.getTotalAmount());
        paymentRequest.setPaymentMethod("UPI");

        ResponseEntity<PaymentResponseDTO> paymentResponse;

        try {
            paymentResponse = restTemplate.exchange(
                    "http://localhost:8083/payments/initiate",
                    HttpMethod.POST,
                    new HttpEntity<>(paymentRequest, headers),
                    PaymentResponseDTO.class
            );
        } catch (HttpClientErrorException e) {

            if(e.getResponseBodyAsString().contains("already exists")){
                savedOrder.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(savedOrder);

                reduceInventory(cartItems, headers);
                cartItemRepository.deleteAll(cartItems);

                return orderMapper.toDTO(savedOrder);
            }

            throw new PaymentFailedException("Payment service failed");
        }

        PaymentResponseDTO paymentBody = paymentResponse.getBody();

        if(paymentBody != null && "SUCCESS".equals(paymentBody.getStatus())){

            savedOrder.setStatus(OrderStatus.CONFIRMED);

            reduceInventory(cartItems, headers);

            cartItemRepository.deleteAll(cartItems);

        } else {
            savedOrder.setStatus(OrderStatus.CANCELLED);
        }

        orderRepository.save(savedOrder);

        return orderMapper.toDTO(savedOrder);
    }

    private void reduceInventory(List<CartItem> cartItems, HttpHeaders headers){

        for(CartItem c : cartItems){

            String reduceUrl = "http://localhost:8082/inventory/reduce/"
                    + c.getProductId() + "?quantity=" + c.getQuantity();

            try {
                restTemplate.exchange(
                        reduceUrl,
                        HttpMethod.PUT,
                        new HttpEntity<>(headers),
                        Void.class
                );
            } catch (Exception e) {
                throw new StockUpdateException("Failed to update stock");
            }
        }
    }

    public List<OrderResponseDTO> getOrders(String username){
        return orderRepository.findByUserId(username)
                .stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    public OrderResponseDTO cancelOrder(Long id){

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        order.setStatus(OrderStatus.CANCELLED);

        return orderMapper.toDTO(orderRepository.save(order));
    }
}