package com.ecommerce.inventory_service.service;

import com.ecommerce.inventory_service.dto.ProductRequestDTO;
import com.ecommerce.inventory_service.dto.ProductResponseDTO;
import com.ecommerce.inventory_service.entity.Product;
import com.ecommerce.inventory_service.entity.Stock;
import com.ecommerce.inventory_service.exception.OutOfStockException;
import com.ecommerce.inventory_service.exception.ResourceNotFoundException;
import com.ecommerce.inventory_service.repository.ProductRepository;
import com.ecommerce.inventory_service.repository.StockRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockRepository stockRepository;

    // ADD PRODUCT
    @Transactional
    public ProductResponseDTO addProduct(ProductRequestDTO dto){

        if(dto.getName() == null || dto.getName().trim().isEmpty()){
            throw new IllegalArgumentException("Product name cannot be empty");
        }

        if(dto.getPrice() <= 0){
            throw new IllegalArgumentException("Price must be greater than 0");
        }

        // NORMALIZATION
        String normalizedName = dto.getName().trim().toLowerCase();

        // DUPLICATE CHECK
        if(productRepository.existsByNormalizedName(normalizedName)){
            throw new IllegalArgumentException("Product already exists");
        }

        Product product = new Product();
        product.setName(dto.getName().trim()); // original
        product.setNormalizedName(normalizedName); // normalized
        product.setPrice(dto.getPrice());
        product.setDetails(dto.getDetails());

        Product savedProduct = productRepository.save(product);

        Stock stock = new Stock();
        stock.setProduct(savedProduct);
        stock.setAvailableQuantity(0);

        stockRepository.save(stock);

        return convertToDTO(savedProduct, stock);
    }

    // GET ALL PRODUCTS
    public List<ProductResponseDTO> getAllProducts(){

        return productRepository.findAll().stream().map(product -> {

            Stock stock = stockRepository.findByProduct(product)
                    .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));

            return convertToDTO(product, stock);

        }).collect(Collectors.toList());
    }

    // GET SINGLE PRODUCT
    public ProductResponseDTO getProduct(Long productId){

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Stock stock = stockRepository.findByProduct(product)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));

        return convertToDTO(product, stock);
    }

    // DELETE PRODUCT
    @Transactional
    public void deleteProduct(Long productId){

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        productRepository.delete(product);
    }

    // GET STOCK
    public Stock getStock(Long productId){

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return stockRepository.findByProduct(product)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));
    }

    // UPDATE STOCK
    @Transactional
    public synchronized Stock updateStock(Long productId, int quantity){

        if(quantity < 0){
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Stock stock = stockRepository.findByProduct(product)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));

        stock.setAvailableQuantity(quantity);

        return stockRepository.save(stock);
    }

    // INCREASE STOCK
    @Transactional
    public synchronized Stock increaseStock(Long productId, int quantity){

        if(quantity <= 0){
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Stock stock = stockRepository.findByProduct(product)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));

        stock.setAvailableQuantity(stock.getAvailableQuantity() + quantity);

        return stockRepository.save(stock);
    }

    // REDUCE STOCK
    @Transactional
    public synchronized String reduceStock(Long productId, int quantity){

        if(quantity <= 0){
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Stock stock = stockRepository.findByProduct(product)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));

        if(stock.getAvailableQuantity() < quantity){
            throw new OutOfStockException("Not enough stock available");
        }

        stock.setAvailableQuantity(stock.getAvailableQuantity() - quantity);

        stockRepository.save(stock);

        return "Stock updated successfully";
    }

    // CHECK STOCK
    public boolean checkStock(Long productId, int quantity){

        if(quantity <= 0){
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Stock stock = stockRepository.findByProduct(product)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));

        return stock.getAvailableQuantity() >= quantity;
    }

    // DTO CONVERSION
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