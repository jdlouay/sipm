package com.sipm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "order_items")
@EqualsAndHashCode(exclude = "order")
@ToString(exclude = "order")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "article_code")
    private String articleCode;

    @Column(name = "article_name", nullable = false)
    private String articleName;

    @Column(nullable = false)
    @Min(value = 1, message = "La quantité doit être supérieure à 0")
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    @NotNull(message = "Le prix unitaire est obligatoire")
    private BigDecimal unitPrice;

    @Column(name = "discount_percentage")
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_percentage")
    private BigDecimal taxPercentage = BigDecimal.ZERO;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @PrePersist
    @PreUpdate
    public void calculateAmounts() {
        // Calculer le sous-total avant remise
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        // Calculer la remise
        if (discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            this.discountAmount = subtotal.multiply(discountPercentage)
                    .divide(BigDecimal.valueOf(100));
        }

        // Calculer la taxe
        if (taxPercentage.compareTo(BigDecimal.ZERO) > 0) {
            this.taxAmount = subtotal.subtract(discountAmount)
                    .multiply(taxPercentage)
                    .divide(BigDecimal.valueOf(100));
        }

        // Calculer le montant total
        this.totalAmount = subtotal
                .subtract(discountAmount)
                .add(taxAmount);
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public String getArticleCode() {
        return articleCode;
    }

    public void setArticleCode(String articleCode) {
        this.articleCode = articleCode;
    }

    public String getArticleName() {
        return articleName;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
} 