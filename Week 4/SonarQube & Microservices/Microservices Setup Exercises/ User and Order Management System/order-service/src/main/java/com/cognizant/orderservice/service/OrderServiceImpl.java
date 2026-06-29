package com.cognizant.orderservice.service;

import com.cognizant.orderservice.client.UserFeignClient;
import com.cognizant.orderservice.client.UserWebClient;
import com.cognizant.orderservice.dto.OrderDto;
import com.cognizant.orderservice.dto.OrderResponseDto;
import com.cognizant.orderservice.dto.UserDto;
import com.cognizant.orderservice.entity.Order;
import com.cognizant.orderservice.exception.ResourceNotFoundException;
import com.cognizant.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final UserFeignClient userFeignClient;
    private final UserWebClient userWebClient;

    // Explicit constructor injection
    public OrderServiceImpl(OrderRepository orderRepository, UserFeignClient userFeignClient, UserWebClient userWebClient) {
        this.orderRepository = orderRepository;
        this.userFeignClient = userFeignClient;
        this.userWebClient = userWebClient;
    }

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderDto orderDto, String clientType) {
        log.info("Creating order with number: {} using clientType: {}", orderDto.getOrderNumber(), clientType);
        
        // 1. Fetch user details first (validates user existence)
        UserDto userDto = fetchUser(orderDto.getUserId(), clientType);
        log.debug("User validated successfully: {}", userDto);

        // 2. Map and save order
        Order order = mapToEntity(orderDto);
        order.setOrderDate(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        log.debug("Successfully saved order with ID: {}", savedOrder.getId());

        return mapToResponseDto(savedOrder, userDto);
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrder(Long id, OrderDto orderDto, String clientType) {
        log.info("Updating order with ID: {} using clientType: {}", id, clientType);
        
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order not found with ID: {}", id);
                    return new ResourceNotFoundException("Order not found with ID: " + id);
                });

        // Validate user existence for the updated order
        UserDto userDto = fetchUser(orderDto.getUserId(), clientType);
        log.debug("User validated successfully: {}", userDto);

        existingOrder.setOrderNumber(orderDto.getOrderNumber());
        existingOrder.setAmount(orderDto.getAmount());
        existingOrder.setUserId(orderDto.getUserId());

        Order updatedOrder = orderRepository.save(existingOrder);
        log.debug("Successfully updated order with ID: {}", updatedOrder.getId());

        return mapToResponseDto(updatedOrder, userDto);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        log.info("Deleting order with ID: {}", id);
        if (!orderRepository.existsById(id)) {
            log.error("Order not found with ID: {}", id);
            throw new ResourceNotFoundException("Order not found with ID: " + id);
        }
        orderRepository.deleteById(id);
        log.debug("Successfully deleted order with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long id, String clientType) {
        log.info("Fetching order with ID: {} using clientType: {}", id, clientType);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order not found with ID: {}", id);
                    return new ResourceNotFoundException("Order not found with ID: " + id);
                });

        UserDto userDto = null;
        try {
            userDto = fetchUser(order.getUserId(), clientType);
        } catch (Exception e) {
            log.warn("Could not retrieve user details for User ID {} during getOrderById: {}", order.getUserId(), e.getMessage());
        }

        return mapToResponseDto(order, userDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders(String clientType) {
        log.info("Fetching all orders using clientType: {}", clientType);
        List<Order> orders = orderRepository.findAll();
        log.debug("Found {} orders in database", orders.size());
        
        return orders.stream()
                .map(order -> {
                    UserDto userDto = null;
                    try {
                        userDto = fetchUser(order.getUserId(), clientType);
                    } catch (Exception e) {
                        log.warn("Could not retrieve user details for User ID {} during getAllOrders: {}", order.getUserId(), e.getMessage());
                    }
                    return mapToResponseDto(order, userDto);
                })
                .collect(Collectors.toList());
    }

    // Helper to fetch user based on selected client type
    private UserDto fetchUser(Long userId, String clientType) {
        if ("webclient".equalsIgnoreCase(clientType)) {
            log.debug("Fetching user via WebClient for ID: {}", userId);
            return userWebClient.getUserById(userId);
        } else {
            log.debug("Fetching user via OpenFeign for ID: {}", userId);
            return userFeignClient.getUserById(userId);
        }
    }

    // Helper: Map OrderDto to Order entity without builder
    private Order mapToEntity(OrderDto dto) {
        Order order = new Order();
        order.setId(dto.getId());
        order.setOrderNumber(dto.getOrderNumber());
        order.setAmount(dto.getAmount());
        order.setUserId(dto.getUserId());
        return order;
    }

    // Helper: Map Order entity + UserDto to OrderResponseDto without builder
    private OrderResponseDto mapToResponseDto(Order order, UserDto userDto) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setAmount(order.getAmount());
        dto.setOrderDate(order.getOrderDate());
        dto.setUser(userDto);
        return dto;
    }
}
