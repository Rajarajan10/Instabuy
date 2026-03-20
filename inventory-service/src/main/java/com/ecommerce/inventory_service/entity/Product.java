package com.ecommerce.inventory_service.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    private String name;

    // case-insensitive uniqueness
    @Column(unique = true, nullable = false)
    private String normalizedName;

    private double price;

    private String details;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Stock stock;

    public Product() {}

    public Long getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public void setNormalizedName(String normalizedName) {
        this.normalizedName = normalizedName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }
}