package com.ecommerce.order_service.mapper;

import com.ecommerce.order_service.dto.*;
import com.ecommerce.order_service.model.*;

import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponseDTO toDTO(Order order){

        OrderResponseDTO dto = new OrderResponseDTO();

        dto.setOrderId(order.getOrderId());
        dto.setUserId(order.getUserId());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setOrderDate(order.getOrderDate());

        dto.setItems(order.getItems().stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList()));

        return dto;
    }

    public OrderItemResponseDTO toItemDTO(OrderItem item){

        OrderItemResponseDTO dto = new OrderItemResponseDTO();
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());

        return dto;
    }
}