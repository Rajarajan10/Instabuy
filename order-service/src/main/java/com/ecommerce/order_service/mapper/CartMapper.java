package com.ecommerce.order_service.mapper;

import com.ecommerce.order_service.dto.*;
import com.ecommerce.order_service.model.*;

import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartResponseDTO toDTO(Cart cart){

        CartResponseDTO dto = new CartResponseDTO();
        dto.setCartId(cart.getCartId());
        dto.setUserId(cart.getUserId());
        dto.setCreatedAt(cart.getCreatedAt());

        dto.setItems(cart.getItems().stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList()));

        return dto;
    }

    public CartItemResponseDTO toItemDTO(CartItem item){

        CartItemResponseDTO dto = new CartItemResponseDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());

        return dto;
    }
}