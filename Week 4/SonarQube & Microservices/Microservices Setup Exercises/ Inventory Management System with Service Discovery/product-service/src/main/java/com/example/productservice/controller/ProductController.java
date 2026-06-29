package com.example.productservice.controller;

import com.example.productservice.dto.ProductRequest;
import com.example.productservice.dto.ProductResponse;
import com.example.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        log.info("START: POST /products - Request: {}", productRequest);
        ProductResponse response = productService.createProduct(productRequest);
        log.info("END: POST /products - Created product ID: {}", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.info("START: GET /products");
        List<ProductResponse> responses = productService.getAllProducts();
        log.info("END: GET /products - Count: {}", responses.size());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        log.info("START: GET /products/{}", id);
        ProductResponse response = productService.getProductById(id);
        log.info("END: GET /products/{} - Product name: {}", id, response.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest productRequest) {
        log.info("START: PUT /products/{} - Request: {}", id, productRequest);
        ProductResponse response = productService.updateProduct(id, productRequest);
        log.info("END: PUT /products/{} - Updated", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("START: DELETE /products/{}", id);
        productService.deleteProduct(id);
        log.info("END: DELETE /products/{}", id);
        return ResponseEntity.noContent().build();
    }
}
