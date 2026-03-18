package com.ecommerce.order_service.service;

import com.ecommerce.order_service.exception.CartNotFoundException;
import com.ecommerce.order_service.model.Cart;
import com.ecommerce.order_service.model.CartItem;
import com.ecommerce.order_service.repository.CartItemRepository;
import com.ecommerce.order_service.repository.CartRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final RestTemplate restTemplate;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       RestTemplate restTemplate){

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.restTemplate = restTemplate;
    }

    public Cart getCart(String username){

        return cartRepository.findByUserId(username)
                .orElseThrow(() -> new CartNotFoundException("CART_NOT_FOUND"));
    }

    public Cart addItem(String username, CartItem item){

        // ---------- CHECK PRODUCT EXISTS IN INVENTORY ----------
        String productUrl = "http://localhost:8082/inventory/product/" + item.getProductId();

        try{
            restTemplate.getForObject(productUrl, Object.class);
        }catch(Exception e){
            throw new RuntimeException("Product does not exist in inventory");
        }


        Cart cart = cartRepository.findByUserId(username)
                .orElseGet(() -> {

                    Cart newCart = new Cart();
                    newCart.setUserId(username);
                    newCart.setCreatedAt(LocalDateTime.now());

                    return cartRepository.save(newCart);
                });


        // ---------- CHECK IF PRODUCT ALREADY EXISTS IN CART ----------
        Optional<CartItem> existingItem =
                cartItemRepository.findByCartAndProductId(cart, item.getProductId());

        if(existingItem.isPresent()){

            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());

            cartItemRepository.save(cartItem);

        }else{

            item.setCart(cart);
            cartItemRepository.save(item);
        }

        return cart;
    }

    public void removeItem(Long itemId){

        cartItemRepository.deleteById(itemId);
    }
}