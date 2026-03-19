package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.*;
import com.ecommerce.order_service.mapper.OrderMapper;
import com.ecommerce.order_service.model.*;
import com.ecommerce.order_service.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.*;

import jakarta.servlet.http.HttpServletRequest;

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

    public OrderResponseDTO checkout(String username, HttpServletRequest request){

        String authHeader = request.getHeader("Authorization");

        // GET CART
        Cart cart = cartRepository.findByUserId(username)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        if(cartItems.isEmpty()){
            throw new RuntimeException("Cart is empty");
        }

        // CREATE ORDER
        Order order = new Order();
        order.setUserId(username);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        double total = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);

        // CHECK INVENTORY
        for(CartItem c : cartItems){

            String checkUrl = "http://localhost:8082/inventory/check/"
                    + c.getProductId() + "?quantity=" + c.getQuantity();

            Boolean available = restTemplate.exchange(
                    checkUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Boolean.class
            ).getBody();

            if(Boolean.FALSE.equals(available)){
                throw new RuntimeException("Not enough stock for product " + c.getProductId());
            }

            // CREATE ORDER ITEM
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

        // SAVE ORDER FIRST
        Order savedOrder = orderRepository.save(order);

        // CALL PAYMENT SERVICE
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

            // HANDLE PAYMENT ALREADY EXISTS
            if(e.getResponseBodyAsString().contains("already exists")){
                savedOrder.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(savedOrder);

                // still reduce inventory + clear cart
                reduceInventory(cartItems, headers);
                cartItemRepository.deleteAll(cartItems);

                return orderMapper.toDTO(savedOrder);
            }

            throw new RuntimeException("Payment service failed");
        }

        PaymentResponseDTO paymentBody = paymentResponse.getBody();

        // HANDLE PAYMENT RESULT
        if(paymentBody != null && "SUCCESS".equals(paymentBody.getStatus())){

            savedOrder.setStatus(OrderStatus.CONFIRMED);

            // REDUCE INVENTORY
            reduceInventory(cartItems, headers);

            // CLEAR CART
            cartItemRepository.deleteAll(cartItems);

        } else {
            savedOrder.setStatus(OrderStatus.CANCELLED);
        }

        orderRepository.save(savedOrder);



        return orderMapper.toDTO(savedOrder);
    }

    // INVENTORY REDUCTION METHOD
    private void reduceInventory(List<CartItem> cartItems, HttpHeaders headers){

        for(CartItem c : cartItems){

            String reduceUrl = "http://localhost:8082/inventory/reduce/"
                    + c.getProductId() + "?quantity=" + c.getQuantity();

            restTemplate.exchange(
                    reduceUrl,
                    HttpMethod.PUT,
                    new HttpEntity<>(headers),
                    Void.class
            );
        }
    }

    // GET ORDERS
    public List<OrderResponseDTO> getOrders(String username){
        return orderRepository.findByUserId(username)
                .stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
    }

    // CANCEL ORDER
    public OrderResponseDTO cancelOrder(Long id){

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.CANCELLED);

        return orderMapper.toDTO(orderRepository.save(order));
    }
}