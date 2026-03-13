package com.ecommerce.order_service.service;

import com.ecommerce.order_service.exception.CartNotFoundException;
import com.ecommerce.order_service.model.Cart;
import com.ecommerce.order_service.model.CartItem;
import com.ecommerce.order_service.repository.CartItemRepository;
import com.ecommerce.order_service.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository){

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public Cart getCart(String username){

        return cartRepository.findByUserId(username)
                .orElseThrow(() -> new CartNotFoundException("CART_NOT_FOUND"));
    }

    public Cart addItem(String username, CartItem item){

        Cart cart = cartRepository.findByUserId(username)
                .orElseGet(() -> {

                    Cart newCart = new Cart();
                    newCart.setUserId(username);
                    newCart.setCreatedAt(LocalDateTime.now());

                    return cartRepository.save(newCart);
                });

        item.setCart(cart);

        cartItemRepository.save(item);

        return cart;
    }

    public void removeItem(Long itemId){

        cartItemRepository.deleteById(itemId);
    }
}