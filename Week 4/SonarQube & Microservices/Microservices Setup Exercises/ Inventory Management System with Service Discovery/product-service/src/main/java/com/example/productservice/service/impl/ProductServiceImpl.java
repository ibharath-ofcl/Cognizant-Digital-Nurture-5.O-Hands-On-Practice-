package com.example.productservice.service.impl;

import com.example.productservice.dto.ProductRequest;
import com.example.productservice.dto.ProductResponse;
import com.example.productservice.entity.Product;
import com.example.productservice.exception.ProductNotFoundException;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest) {
        log.info("START: createProduct - Request: {}", productRequest);
        
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .stockQuantity(productRequest.getStockQuantity())
                .build();
        
        Product savedProduct = productRepository.save(product);
        log.debug("createProduct - Saved product in database with ID: {}", savedProduct.getId());
        
        ProductResponse response = mapToResponse(savedProduct);
        log.info("END: createProduct - Created response: {}", response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        log.info("START: getAllProducts");
        List<Product> products = productRepository.findAll();
        List<ProductResponse> responses = products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        log.info("END: getAllProducts - Retrieved {} products", responses.size());
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.info("START: getProductById - ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("getProductById - Product not found for ID: {}", id);
                    return new ProductNotFoundException("Product not found with ID: " + id);
                });
        ProductResponse response = mapToResponse(product);
        log.info("END: getProductById - Response: {}", response);
        return response;
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
        log.info("START: updateProduct - ID: {}, Request: {}", id, productRequest);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("updateProduct - Product not found for ID: {}", id);
                    return new ProductNotFoundException("Product not found with ID: " + id);
                });

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setStockQuantity(productRequest.getStockQuantity());

        Product updatedProduct = productRepository.save(product);
        log.debug("updateProduct - Product updated successfully in database");
        
        ProductResponse response = mapToResponse(updatedProduct);
        log.info("END: updateProduct - Response: {}", response);
        return response;
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("START: deleteProduct - ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("deleteProduct - Product not found for ID: {}", id);
                    return new ProductNotFoundException("Product not found with ID: " + id);
                });
        productRepository.delete(product);
        log.info("END: deleteProduct - Successfully deleted product ID: {}", id);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();
    }
}
