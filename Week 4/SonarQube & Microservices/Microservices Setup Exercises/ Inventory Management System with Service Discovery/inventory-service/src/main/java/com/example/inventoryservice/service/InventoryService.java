package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.InventoryRequest;
import com.example.inventoryservice.dto.InventoryResponse;
import com.example.inventoryservice.dto.StockCheckResponse;

public interface InventoryService {
    InventoryResponse addInventory(InventoryRequest request);
    InventoryResponse getInventoryByProductId(Long productId);
    InventoryResponse updateInventory(Long id, InventoryRequest request);
    void deleteInventory(Long id);
    StockCheckResponse checkStock(Long productId);
    InventoryResponse reduceStock(Long productId, Integer quantity);
    InventoryResponse increaseStock(Long productId, Integer quantity);
}
