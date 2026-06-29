package com.cognizant.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDto {

    private Long id;

    @NotBlank(message = "Order number is required")
    private String orderNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be a positive value")
    private BigDecimal amount;

    private LocalDateTime orderDate;

    @NotNull(message = "User ID is required")
    private Long userId;

    // Constructors
    public OrderDto() {
    }

    public OrderDto(Long id, String orderNumber, BigDecimal amount, LocalDateTime orderDate, Long userId) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.amount = amount;
        this.orderDate = orderDate;
        this.userId = userId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "OrderDto{" +
                "id=" + id +
                ", orderNumber='" + orderNumber + '\'' +
                ", amount=" + amount +
                ", orderDate=" + orderDate +
                ", userId=" + userId +
                '}';
    }
}
