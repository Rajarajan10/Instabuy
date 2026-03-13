package com.ecommerce.inventory_service.repository;

import com.ecommerce.inventory_service.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {
}