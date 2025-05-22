package com.sipm.model;

public class OrderWithInvoiceDTO {
    private Order order;
    private boolean invoiceGenerated;
    private Long invoiceId;

    public OrderWithInvoiceDTO(Order order, boolean invoiceGenerated, Long invoiceId) {
        this.order = order;
        this.invoiceGenerated = invoiceGenerated;
        this.invoiceId = invoiceId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public boolean isInvoiceGenerated() {
        return invoiceGenerated;
    }

    public void setInvoiceGenerated(boolean invoiceGenerated) {
        this.invoiceGenerated = invoiceGenerated;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }
} 