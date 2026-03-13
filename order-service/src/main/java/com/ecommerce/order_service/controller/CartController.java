package com.ecommerce.order_service.controller;

import com.ecommerce.order_service.model.Cart;
import com.ecommerce.order_service.model.CartItem;
import com.ecommerce.order_service.service.CartService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService){
        this.cartService = cartService;
    }

        @PostMapping("/add")
        public Cart addItem(Authentication authentication,
                            @RequestBody CartItem item){

            String username = SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName();

            return cartService.addItem(username,item);
        }

    @GetMapping
    public Cart getCart(Authentication authentication){

        String username = authentication.getName();

        return cartService.getCart(username);
    }

    @DeleteMapping("/remove/{itemId}")
    public String removeItem(@PathVariable Long itemId){

        cartService.removeItem(itemId);

        return "Item removed from cart";
    }
}