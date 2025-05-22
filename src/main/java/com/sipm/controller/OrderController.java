package com.sipm.controller;

import com.sipm.model.Order;
import com.sipm.model.OrderWithInvoiceDTO;
import com.sipm.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderService.getAllOrders(pageable);
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        return orderService.createOrder(order);
    }

    @PutMapping("/{id}/status")
    public Order updateOrderStatus(@PathVariable Long id, @RequestParam Order.OrderStatus status) {
        return orderService.updateOrderStatus(id, status);
    }

    @PutMapping("/{id}/payment-status")
    public Order updatePaymentStatus(@PathVariable Long id, @RequestParam Order.PaymentStatus status) {
        return orderService.updatePaymentStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public void cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
    }

    @GetMapping("/status")
    public List<Order> getOrdersByStatusAndDateRange(
            @RequestParam Order.OrderStatus status,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        // Conversion des dates à LocalDateTime à faire côté service si besoin
        return orderService.getOrdersByStatusAndDateRange(status, null, null);
    }

    @GetMapping("/payment-status")
    public List<Order> getOrdersByPaymentStatusAndDateRange(
            @RequestParam Order.PaymentStatus status,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        // Conversion des dates à LocalDateTime à faire côté service si besoin
        return orderService.getOrdersByPaymentStatusAndDateRange(status, null, null);
    }

    @GetMapping("/with-invoice")
    public List<OrderWithInvoiceDTO> getAllOrdersWithInvoiceInfo() {
        return orderService.getAllOrdersWithInvoiceInfo();
    }
} 