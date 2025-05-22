package com.sipm.service;

import com.sipm.model.Invoice;
import com.sipm.model.Order;
import com.sipm.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;

    public Invoice generateInvoice(Order order) {
        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setInvoiceDate(LocalDateTime.now());
        invoice.setTotalAmount(order.getFinalAmount());
        return invoiceRepository.save(invoice);
    }
} 