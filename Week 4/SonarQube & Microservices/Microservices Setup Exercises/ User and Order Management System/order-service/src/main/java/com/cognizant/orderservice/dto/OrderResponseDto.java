package com.cognizant.orderservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderResponseDto {
    private Long id;
    private String orderNumber;
    private BigDecimal amount;
    private LocalDateTime orderDate;
    private UserDto user;

    // Constructors
    public OrderResponseDto() {
    }

    public OrderResponseDto(Long id, String orderNumber, BigDecimal amount, LocalDateTime orderDate, UserDto user) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.amount = amount;
        this.orderDate = orderDate;
        this.user = user;
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

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "OrderResponseDto{" +
                "id=" + id +
                ", orderNumber='" + orderNumber + '\'' +
                ", amount=" + amount +
                ", orderDate=" + orderDate +
                ", user=" + user +
                '}';
    }
}
