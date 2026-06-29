package com.cognizant.orderservice.service;

import com.cognizant.orderservice.dto.OrderDto;
import com.cognizant.orderservice.dto.OrderResponseDto;

import java.util.List;

public interface OrderService {
    OrderResponseDto createOrder(OrderDto orderDto, String clientType);
    OrderResponseDto updateOrder(Long id, OrderDto orderDto, String clientType);
    void deleteOrder(Long id);
    OrderResponseDto getOrderById(Long id, String clientType);
    List<OrderResponseDto> getAllOrders(String clientType);
}
