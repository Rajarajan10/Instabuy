package com.ecommerce.order_service.controller;

import com.ecommerce.order_service.dto.OrderResponseDTO;
import com.ecommerce.order_service.service.OrderService;

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
    public OrderResponseDTO checkout(
            @RequestHeader("X-User-Name") String username,
            @RequestHeader(value = "Authorization", required = false) String authHeader){

        return orderService.checkout(username, authHeader);
    }

    @GetMapping
    public List<OrderResponseDTO> getOrders(
            @RequestHeader("X-User-Name") String username){

        return orderService.getOrders(username);
    }

    @PutMapping("/cancel/{orderId}")
    public OrderResponseDTO cancelOrder(@PathVariable Long orderId){
        return orderService.cancelOrder(orderId);
    }
}