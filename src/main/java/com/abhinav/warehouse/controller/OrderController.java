package com.abhinav.warehouse.controller;

import com.abhinav.warehouse.dto.*;
import com.abhinav.warehouse.repository.FulfillmentLogRepository;
import com.abhinav.warehouse.repository.OrderRepository;
import com.abhinav.warehouse.service.FulfillmentService;
import com.abhinav.warehouse.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final FulfillmentService fulfillmentService;
    private final OrderRepository orderRepository;
    private final FulfillmentLogRepository logRepository;

    @PostMapping
    public ResponseEntity<OrderResponse> place(@Valid @RequestBody PlaceOrderRequest req) {
        Long orderId = orderService.placeOrder(req);
        FulfillmentService.Result result = fulfillmentService.fulfil(orderId);
        var order = orderRepository.findById(orderId).orElseThrow();
        return ResponseEntity.ok(OrderResponse.from(order, result.attemptsUsed()));
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return OrderResponse.from(orderRepository.findById(id).orElseThrow(), 0);
    }

    /** Feeds the decision-trace screen. */
    @GetMapping("/{id}/trace")
    public List<TraceStepResponse> trace(@PathVariable Long id) {
        return logRepository.findByOrderIdOrderByCreatedAtAsc(id)
                .stream().map(TraceStepResponse::from).toList();
    }
}
