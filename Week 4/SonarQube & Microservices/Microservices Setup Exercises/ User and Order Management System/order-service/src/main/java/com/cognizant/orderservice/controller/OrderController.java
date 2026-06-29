package com.cognizant.orderservice.controller;

import com.cognizant.orderservice.dto.OrderDto;
import com.cognizant.orderservice.dto.OrderResponseDto;
import com.cognizant.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    // Explicit constructor injection
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody OrderDto orderDto,
            @RequestParam(value = "clientType", defaultValue = "feign") String clientType) {
        log.info("REST Request - Create Order using clientType: {}, Payload: {}", clientType, orderDto);
        OrderResponseDto createdOrder = orderService.createOrder(orderDto, clientType);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDto> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderDto orderDto,
            @RequestParam(value = "clientType", defaultValue = "feign") String clientType) {
        log.info("REST Request - Update Order ID: {} using clientType: {}, Payload: {}", id, clientType, orderDto);
        OrderResponseDto updatedOrder = orderService.updateOrder(id, orderDto, clientType);
        return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        log.info("REST Request - Delete Order ID: {}", id);
        orderService.deleteOrder(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(
            @PathVariable Long id,
            @RequestParam(value = "clientType", defaultValue = "feign") String clientType) {
        log.info("REST Request - Get Order ID: {} using clientType: {}", id, clientType);
        OrderResponseDto orderResponse = orderService.getOrderById(id, clientType);
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getAllOrders(
            @RequestParam(value = "clientType", defaultValue = "feign") String clientType) {
        log.info("REST Request - Get All Orders using clientType: {}", clientType);
        List<OrderResponseDto> orders = orderService.getAllOrders(clientType);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }
}
