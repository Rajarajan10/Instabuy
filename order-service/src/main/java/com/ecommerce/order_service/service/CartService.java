package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.*;
import com.ecommerce.order_service.exception.CartNotFoundException;
import com.ecommerce.order_service.mapper.CartMapper;
import com.ecommerce.order_service.model.*;
import com.ecommerce.order_service.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final RestTemplate restTemplate;
    private final CartMapper cartMapper;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       RestTemplate restTemplate,
                       CartMapper cartMapper){

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.restTemplate = restTemplate;
        this.cartMapper = cartMapper;
    }

    // Get Cart
    public CartResponseDTO getCart(String username){

        Cart cart = cartRepository.findByUserId(username)
                .orElseThrow(() -> new CartNotFoundException("CART_NOT_FOUND"));

        return cartMapper.toDTO(cart);
    }

    // Add Item to Cart
    public CartResponseDTO addItem(String username, CartItemRequest request){

        // Fetch product details (price comes from inventory service)
        String productUrl = "http://localhost:8082/inventory/product/" + request.getProductId();

        ProductResponseDTO product = restTemplate.getForObject(
                productUrl,
                ProductResponseDTO.class
        );

        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        // Get or create cart
        Cart cart = cartRepository.findByUserId(username)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUserId(username);
                    c.setCreatedAt(LocalDateTime.now());
                    return cartRepository.save(c);
                });

        // Check if item already exists
        Optional<CartItem> existing =
                cartItemRepository.findByCartAndProductId(cart, request.getProductId());

        int totalQuantity = request.getQuantity();

        if(existing.isPresent()){
            totalQuantity += existing.get().getQuantity();
        }

        // Check stock availability using total quantity
        String stockCheckUrl = "http://localhost:8082/inventory/check/"
                + request.getProductId()
                + "?quantity=" + totalQuantity;

        Boolean isAvailable = restTemplate.getForObject(stockCheckUrl, Boolean.class);

        if (Boolean.FALSE.equals(isAvailable)) {
            throw new RuntimeException("Not enough stock available");
        }

        // Add or update cart item with correct price
        if(existing.isPresent()){
            CartItem item = existing.get();
            item.setQuantity(totalQuantity);
            item.setPrice(product.getPrice()); // price from inventory
            cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setProductId(request.getProductId());
            item.setQuantity(request.getQuantity());
            item.setPrice(product.getPrice()); // price from inventory
            item.setCart(cart);
            cartItemRepository.save(item);
        }

        // Return updated cart
        return cartMapper.toDTO(cart);
    }

    // Remove item from cart
    public void removeItem(Long itemId){
        cartItemRepository.deleteById(itemId);
    }
}