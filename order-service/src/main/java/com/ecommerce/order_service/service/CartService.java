package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.*;
import com.ecommerce.order_service.exception.*;
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

    public CartResponseDTO getCart(String username){

        Cart cart = cartRepository.findByUserId(username)
                .orElseThrow(() -> new CartNotFoundException("CART_NOT_FOUND"));

        return cartMapper.toDTO(cart);
    }

    public CartResponseDTO addItem(String username, CartItemRequest request){

        // VALIDATION
        if (request.getProductId() == null) {
            throw new InvalidQuantityException("Product ID cannot be null");
        }

        if (request.getQuantity() <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than zero");
        }

        // FETCH PRODUCT
        String productUrl = "http://localhost:8082/inventory/product/" + request.getProductId();

        ProductResponseDTO product;
        try {
            product = restTemplate.getForObject(productUrl, ProductResponseDTO.class);
        } catch (Exception e) {
            throw new InventoryServiceException("Inventory service unavailable");
        }

        if (product == null) {
            throw new ProductNotFoundException("Product not found");
        }

        // GET OR CREATE CART
        Cart cart = cartRepository.findByUserId(username)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUserId(username);
                    c.setCreatedAt(LocalDateTime.now());
                    return cartRepository.save(c);
                });

        Optional<CartItem> existing =
                cartItemRepository.findByCartAndProductId(cart, request.getProductId());

        int totalQuantity = request.getQuantity();

        if(existing.isPresent()){
            totalQuantity += existing.get().getQuantity();
        }

        // STOCK CHECK
        String stockCheckUrl = "http://localhost:8082/inventory/check/"
                + request.getProductId()
                + "?quantity=" + totalQuantity;

        Boolean isAvailable;
        try {
            isAvailable = restTemplate.getForObject(stockCheckUrl, Boolean.class);
        } catch (Exception e) {
            throw new InventoryServiceException("Inventory service unavailable");
        }

        if (Boolean.FALSE.equals(isAvailable)) {
            throw new InsufficientStockException("Not enough stock available");
        }

        // SAVE
        if(existing.isPresent()){
            CartItem item = existing.get();
            item.setQuantity(totalQuantity);
            item.setPrice(product.getPrice());
            cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setProductId(request.getProductId());
            item.setQuantity(request.getQuantity());
            item.setPrice(product.getPrice());
            item.setCart(cart);
            cartItemRepository.save(item);
        }

        return cartMapper.toDTO(cart);
    }

    public void removeItem(Long itemId){
        cartItemRepository.deleteById(itemId);
    }
}