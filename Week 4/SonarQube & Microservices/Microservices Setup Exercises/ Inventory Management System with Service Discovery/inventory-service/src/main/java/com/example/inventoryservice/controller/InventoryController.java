package com.example.inventoryservice.controller;

import com.example.inventoryservice.client.ProductClient;
import com.example.inventoryservice.dto.InventoryRequest;
import com.example.inventoryservice.dto.InventoryResponse;
import com.example.inventoryservice.dto.ProductResponse;
import com.example.inventoryservice.dto.StockCheckResponse;
import com.example.inventoryservice.entity.Inventory;
import com.example.inventoryservice.repository.InventoryRepository;
import com.example.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryRepository inventoryRepository;
    private final ProductClient productClient;

    @PostMapping
    public ResponseEntity<InventoryResponse> addInventory(@Valid @RequestBody InventoryRequest request) {
        log.info("START: POST /inventory - Request: {}", request);
        InventoryResponse response = inventoryService.addInventory(request);
        log.info("END: POST /inventory - Created ID: {}", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllInventories() {
        log.info("START: GET /inventory");
        List<Inventory> inventories = inventoryRepository.findAll();
        List<InventoryResponse> responses = inventories.stream()
                .map(inv -> {
                    ProductResponse product = null;
                    try {
                        product = productClient.getProductById(inv.getProductId());
                    } catch (Exception ex) {
                        log.warn("GET /inventory - Failed to fetch product ID: {}", inv.getProductId());
                    }
                    return InventoryResponse.builder()
                            .id(inv.getId())
                            .productId(inv.getProductId())
                            .availableQuantity(inv.getAvailableQuantity())
                            .reservedQuantity(inv.getReservedQuantity())
                            .warehouseLocation(inv.getWarehouseLocation())
                            .productName(product != null ? product.getName() : "Unknown Product")
                            .productPrice(product != null ? product.getPrice() : null)
                            .build();
                })
                .collect(Collectors.toList());
        log.info("END: GET /inventory - Count: {}", responses.size());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventoryByProductId(@PathVariable Long productId) {
        log.info("START: GET /inventory/{}", productId);
        InventoryResponse response = inventoryService.getInventoryByProductId(productId);
        log.info("END: GET /inventory/{} - Warehouse: {}", productId, response.getWarehouseLocation());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryResponse> updateInventory(@PathVariable Long id, @Valid @RequestBody InventoryRequest request) {
        log.info("START: PUT /inventory/{} - Request: {}", id, request);
        InventoryResponse response = inventoryService.updateInventory(id, request);
        log.info("END: PUT /inventory/{}", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        log.info("START: DELETE /inventory/{}", id);
        inventoryService.deleteInventory(id);
        log.info("END: DELETE /inventory/{}", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<StockCheckResponse> checkStock(@PathVariable Long productId) {
        log.info("START: GET /inventory/check/{}", productId);
        StockCheckResponse response = inventoryService.checkStock(productId);
        log.info("END: GET /inventory/check/{} - Available: {}", productId, response.isAvailable());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/reduce/{productId}")
    public ResponseEntity<InventoryResponse> reduceStock(@PathVariable Long productId, @RequestParam Integer quantity) {
        log.info("START: PUT /inventory/reduce/{} - Quantity: {}", productId, quantity);
        InventoryResponse response = inventoryService.reduceStock(productId, quantity);
        log.info("END: PUT /inventory/reduce/{}", productId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/increase/{productId}")
    public ResponseEntity<InventoryResponse> increaseStock(@PathVariable Long productId, @RequestParam Integer quantity) {
        log.info("START: PUT /inventory/increase/{} - Quantity: {}", productId, quantity);
        InventoryResponse response = inventoryService.increaseStock(productId, quantity);
        log.info("END: PUT /inventory/increase/{}", productId);
        return ResponseEntity.ok(response);
    }
}
