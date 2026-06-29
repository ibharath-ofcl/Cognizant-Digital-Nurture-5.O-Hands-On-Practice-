package com.example.inventoryservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockCheckResponse {
    private Long productId;
    private Integer availableQuantity;
    private boolean isAvailable;
}
