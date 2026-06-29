package com.example.inventoryservice.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {
    private Long id;
    private Long productId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private String warehouseLocation;
    
    // Product details fetched from Product Service
    private String productName;
    private BigDecimal productPrice;
}
