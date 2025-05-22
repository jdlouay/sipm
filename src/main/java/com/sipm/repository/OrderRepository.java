package com.sipm.repository;

import com.sipm.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Optional<Order> findByOrderNumber(String orderNumber);
    boolean existsByOrderNumber(String orderNumber);
    
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.status = ?1 AND o.orderDate BETWEEN ?2 AND ?3")
    List<Order> findByStatusAndOrderDateBetween(Order.OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.paymentStatus = ?1 AND o.orderDate BETWEEN ?2 AND ?3")
    List<Order> findByPaymentStatusAndOrderDateBetween(Order.PaymentStatus status, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.status = ?1 AND o.orderDate BETWEEN ?2 AND ?3")
    BigDecimal getTotalAmountByStatusAndDateRange(Order.OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);
} 