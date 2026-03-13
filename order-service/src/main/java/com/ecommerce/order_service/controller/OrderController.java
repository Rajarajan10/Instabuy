package com.ecommerce.order_service.controller;

import com.ecommerce.order_service.model.Order;
import com.ecommerce.order_service.service.OrderService;

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

    // create order manually
    @PostMapping
    public Order createOrder(Authentication authentication,
                             @RequestBody Order order){

        String username = authentication.getName();

        return orderService.createOrder(username,order);
    }

    // get orders of logged user
    @GetMapping
    public List<Order> getOrders(Authentication authentication){

        String username = authentication.getName();

        return orderService.getOrders(username);
    }

    // cancel order
    @PutMapping("/cancel/{orderId}")
    public Order cancelOrder(@PathVariable Long orderId){

        return orderService.cancelOrder(orderId);
    }

    // checkout cart -> create order
    @PostMapping("/checkout")
    public Order checkout(Authentication authentication){

        String username = authentication.getName();

        return orderService.checkout(username);
    }
}