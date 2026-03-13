package com.ecommerce.order_service.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="carts")
public class Cart {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long cartId;

    private String userId;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy="cart",cascade=CascadeType.ALL)
    @JsonManagedReference
    private List<CartItem> items;

    public Cart() {
    }

    public Cart(Long cartId, String userId, LocalDateTime createdAt, List<CartItem> items) {
        this.cartId = cartId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.items = items;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
