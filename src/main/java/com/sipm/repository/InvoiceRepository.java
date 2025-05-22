package com.sipm.repository;

import com.sipm.model.Invoice;
import com.sipm.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByOrder(Order order);
} 