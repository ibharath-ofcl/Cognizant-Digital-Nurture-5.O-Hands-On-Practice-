package com.example.inventoryservice.service.impl;

import com.example.inventoryservice.client.ProductClient;
import com.example.inventoryservice.dto.InventoryRequest;
import com.example.inventoryservice.dto.InventoryResponse;
import com.example.inventoryservice.dto.ProductResponse;
import com.example.inventoryservice.dto.StockCheckResponse;
import com.example.inventoryservice.entity.Inventory;
import com.example.inventoryservice.exception.InventoryNotFoundException;
import com.example.inventoryservice.repository.InventoryRepository;
import com.example.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductClient productClient;

    @Override
    @Transactional
    public InventoryResponse addInventory(InventoryRequest request) {
        log.info("START: addInventory - Request: {}", request);
        
        // Inter-service communication: Validate Product Exists
        log.debug("addInventory - Calling Product Service to validate product ID: {}", request.getProductId());
        ProductResponse product = productClient.getProductById(request.getProductId());
        log.debug("addInventory - Product verified: {}", product.getName());

        Inventory inventory = Inventory.builder()
                .productId(request.getProductId())
                .availableQuantity(request.getAvailableQuantity())
                .reservedQuantity(request.getReservedQuantity())
                .warehouseLocation(request.getWarehouseLocation())
                .build();

        Inventory savedInventory = inventoryRepository.save(inventory);
        log.debug("addInventory - Inventory record saved with ID: {}", savedInventory.getId());

        InventoryResponse response = mapToResponse(savedInventory, product);
        log.info("END: addInventory - Response: {}", response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(Long productId) {
        log.info("START: getInventoryByProductId - Product ID: {}", productId);
        
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    log.error("getInventoryByProductId - Inventory record not found for Product ID: {}", productId);
                    return new InventoryNotFoundException("Inventory not found for Product ID: " + productId);
                });

        // Fetch Product Details dynamically from Product Service
        ProductResponse product = null;
        try {
            log.debug("getInventoryByProductId - Calling Product Service for Product ID: {}", productId);
            product = productClient.getProductById(productId);
        } catch (Exception ex) {
            log.warn("getInventoryByProductId - Failed to fetch product details for Product ID: {}. Error: {}", productId, ex.getMessage());
        }

        InventoryResponse response = mapToResponse(inventory, product);
        log.info("END: getInventoryByProductId - Response: {}", response);
        return response;
    }

    @Override
    @Transactional
    public InventoryResponse updateInventory(Long id, InventoryRequest request) {
        log.info("START: updateInventory - ID: {}, Request: {}", id, request);
        
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("updateInventory - Inventory record not found for ID: {}", id);
                    return new InventoryNotFoundException("Inventory record not found with ID: " + id);
                });

        // Inter-service communication: Validate Product Exists
        log.debug("updateInventory - Calling Product Service to validate product ID: {}", request.getProductId());
        ProductResponse product = productClient.getProductById(request.getProductId());

        inventory.setProductId(request.getProductId());
        inventory.setAvailableQuantity(request.getAvailableQuantity());
        inventory.setReservedQuantity(request.getReservedQuantity());
        inventory.setWarehouseLocation(request.getWarehouseLocation());

        Inventory updatedInventory = inventoryRepository.save(inventory);
        log.debug("updateInventory - Inventory record updated successfully in database");

        InventoryResponse response = mapToResponse(updatedInventory, product);
        log.info("END: updateInventory - Response: {}", response);
        return response;
    }

    @Override
    @Transactional
    public void deleteInventory(Long id) {
        log.info("START: deleteInventory - ID: {}", id);
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("deleteInventory - Inventory record not found for ID: {}", id);
                    return new InventoryNotFoundException("Inventory record not found with ID: " + id);
                });
        inventoryRepository.delete(inventory);
        log.info("END: deleteInventory - Successfully deleted inventory ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public StockCheckResponse checkStock(Long productId) {
        log.info("START: checkStock - Product ID: {}", productId);
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for Product ID: " + productId));

        boolean isAvailable = inventory.getAvailableQuantity() > 0;
        StockCheckResponse response = StockCheckResponse.builder()
                .productId(productId)
                .availableQuantity(inventory.getAvailableQuantity())
                .isAvailable(isAvailable)
                .build();
        
        log.info("END: checkStock - Response: {}", response);
        return response;
    }

    @Override
    @Transactional
    public InventoryResponse reduceStock(Long productId, Integer quantity) {
        log.info("START: reduceStock - Product ID: {}, Quantity: {}", productId, quantity);
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for Product ID: " + productId));

        if (inventory.getAvailableQuantity() < quantity) {
            log.error("reduceStock - Insufficient stock for Product ID: {}. Requested: {}, Available: {}", 
                    productId, quantity, inventory.getAvailableQuantity());
            throw new IllegalArgumentException("Insufficient stock. Available: " + inventory.getAvailableQuantity());
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        Inventory updatedInventory = inventoryRepository.save(inventory);
        
        ProductResponse product = null;
        try {
            product = productClient.getProductById(productId);
        } catch (Exception ex) {
            log.warn("reduceStock - Product details retrieval failed: {}", ex.getMessage());
        }

        InventoryResponse response = mapToResponse(updatedInventory, product);
        log.info("END: reduceStock - Response: {}", response);
        return response;
    }

    @Override
    @Transactional
    public InventoryResponse increaseStock(Long productId, Integer quantity) {
        log.info("START: increaseStock - Product ID: {}, Quantity: {}", productId, quantity);
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for Product ID: " + productId));

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        Inventory updatedInventory = inventoryRepository.save(inventory);

        ProductResponse product = null;
        try {
            product = productClient.getProductById(productId);
        } catch (Exception ex) {
            log.warn("increaseStock - Product details retrieval failed: {}", ex.getMessage());
        }

        InventoryResponse response = mapToResponse(updatedInventory, product);
        log.info("END: increaseStock - Response: {}", response);
        return response;
    }

    private InventoryResponse mapToResponse(Inventory inventory, ProductResponse product) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .availableQuantity(inventory.getAvailableQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .warehouseLocation(inventory.getWarehouseLocation())
                .productName(product != null ? product.getName() : "Unknown Product")
                .productPrice(product != null ? product.getPrice() : null)
                .build();
    }
}
