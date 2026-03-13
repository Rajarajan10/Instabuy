package com.ecommerce.order_service.repository;

import com.ecommerce.order_service.model.Cart;
import com.ecommerce.order_service.model.CartItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem,Long> {

    List<CartItem> findByCart(Cart cart);
}