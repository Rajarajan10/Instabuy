package com.ecommerce.inventory_service.service;

import com.ecommerce.inventory_service.dto.ProductRequestDTO;
import com.ecommerce.inventory_service.dto.ProductResponseDTO;
import com.ecommerce.inventory_service.entity.Product;
import com.ecommerce.inventory_service.entity.Stock;
import com.ecommerce.inventory_service.exception.ResourceNotFoundException;
import com.ecommerce.inventory_service.repository.ProductRepository;
import com.ecommerce.inventory_service.repository.StockRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockRepository stockRepository;

    // Add product
    public ProductResponseDTO addProduct(ProductRequestDTO dto){

        Product product = new Product();
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setDetails(dto.getDetails());

        Product savedProduct = productRepository.save(product);

        Stock stock = new Stock();
        stock.setProduct(savedProduct);
        stock.setAvailableQuantity(0);

        stockRepository.save(stock);

        return convertToDTO(savedProduct, stock);
    }

    // Get all products
    public List<ProductResponseDTO> getAllProducts(){

        List<Product> products = productRepository.findAll();

        return products.stream().map(product -> {

            Stock stock = stockRepository.findById(product.getProductId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Stock not found"));

            return convertToDTO(product, stock);

        }).collect(Collectors.toList());
    }

    // Get single product
    public ProductResponseDTO getProduct(Long productId){

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        Stock stock = stockRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Stock not found"));

        return convertToDTO(product, stock);
    }

    // Delete product
    public void deleteProduct(Long productId){

        if(!productRepository.existsById(productId)){
            throw new ResourceNotFoundException("Product not found");
        }

        productRepository.deleteById(productId);
    }

    // Get stock
    public Stock getStock(Long productId){

        return stockRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Stock not found"));
    }

    // Update stock
    public Stock updateStock(Long productId, int quantity){

        Stock stock = stockRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Stock not found"));

        stock.setAvailableQuantity(quantity);

        return stockRepository.save(stock);
    }

    // Increase stock
    public Stock increaseStock(Long productId, int quantity){

        Stock stock = stockRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Stock not found"));

        stock.setAvailableQuantity(
                stock.getAvailableQuantity() + quantity
        );

        return stockRepository.save(stock);
    }

    // Reduce stock
    public String reduceStock(Long productId, int quantity){

        Stock stock = stockRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Stock not found"));

        if(stock.getAvailableQuantity() < quantity){
            throw new RuntimeException("Not enough stock available");
        }

        stock.setAvailableQuantity(
                stock.getAvailableQuantity() - quantity
        );

        stockRepository.save(stock);

        return "Stock updated";
    }

    // Check stock availability
    public boolean checkStock(Long productId, int quantity){

        Stock stock = stockRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Stock not found"));

        return stock.getAvailableQuantity() >= quantity;
    }

    // Convert Entity → DTO
    private ProductResponseDTO convertToDTO(Product product, Stock stock){

        ProductResponseDTO dto = new ProductResponseDTO();

        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setDetails(product.getDetails());
        dto.setAvailableQuantity(stock.getAvailableQuantity());

        return dto;
    }
}