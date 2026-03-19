package com.ecommerce.order_service.controller;

import com.ecommerce.order_service.dto.OrderResponseDTO;
import com.ecommerce.order_service.service.OrderService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public OrderResponseDTO checkout(Authentication authentication,
                                     HttpServletRequest request){

        String username = authentication.getName();
        return orderService.checkout(username, request);
    }

    @GetMapping
    public List<OrderResponseDTO> getOrders(Authentication authentication){

        String username = authentication.getName();
        return orderService.getOrders(username);
    }

    @PutMapping("/cancel/{orderId}")
    public OrderResponseDTO cancelOrder(@PathVariable Long orderId){
        return orderService.cancelOrder(orderId);
    }
}