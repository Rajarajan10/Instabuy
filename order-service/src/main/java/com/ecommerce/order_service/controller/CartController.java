package com.ecommerce.order_service.controller;

import com.ecommerce.order_service.dto.CartItemRequest;
import com.ecommerce.order_service.dto.CartResponseDTO;
import com.ecommerce.order_service.service.CartService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService){
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public CartResponseDTO addItem(Authentication authentication,
                                   @RequestBody CartItemRequest request){

        String username = authentication.getName();
        return cartService.addItem(username, request);
    }

    @GetMapping
    public CartResponseDTO getCart(Authentication authentication){

        String username = authentication.getName();
        return cartService.getCart(username);
    }

    @DeleteMapping("/remove/{itemId}")
    public String removeItem(@PathVariable Long itemId){
        cartService.removeItem(itemId);
        return "Item removed from cart";
    }
}