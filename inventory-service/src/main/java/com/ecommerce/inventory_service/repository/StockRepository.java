package com.ecommerce.inventory_service.repository;

import com.ecommerce.inventory_service.entity.Stock;
import com.ecommerce.inventory_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByProduct(Product product);
}