package com.ecommerce.order_service.repository;

import com.ecommerce.order_service.model.Cart;
import com.ecommerce.order_service.model.CartItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem,Long> {

    List<CartItem> findByCart(Cart cart);
    Optional<CartItem> findByCartAndProductId(Cart cart, Long productId);
}