package com.sipm.service;

import com.sipm.common.BusinessException;
import com.sipm.model.Order;
import com.sipm.model.OrderItem;
import com.sipm.model.OrderWithInvoiceDTO;
import com.sipm.repository.OrderRepository;
import com.sipm.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final ExternalServiceClient externalServiceClient;
    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;

    public Order createOrder(Order order) {
        try {
            // Générer un numéro de commande unique
            order.setOrderNumber(generateOrderNumber());
            order.setOrderDate(LocalDateTime.now());

            // Vérifier le client
            ExternalServiceClient.CustomerInfo customerInfo = externalServiceClient.getCustomerInfo(order.getCustomerId());
            if (customerInfo == null) {
                throw new BusinessException("Client non trouvé", HttpStatus.BAD_REQUEST);
            }
            order.setCustomerName(customerInfo.name() + " " + customerInfo.firstName());
            // Vérifier la limite de crédit si nécessaire
            if (order.getFinalAmount().doubleValue() > customerInfo.creditLimit()) {
                throw new BusinessException("La limite de crédit du client est dépassée", HttpStatus.BAD_REQUEST);
            }

            // Vérifier et mettre à jour le stock pour chaque article
            for (OrderItem item : order.getItems()) {
                ExternalServiceClient.ArticleInfo articleInfo = externalServiceClient.getArticleInfo(item.getArticleId());
                if (articleInfo == null) {
                    throw new BusinessException("Article non trouvé", HttpStatus.BAD_REQUEST);
                }
                item.setArticleCode(articleInfo.code());
                item.setArticleName(articleInfo.name());
                // Toujours forcer le prix unitaire depuis l'article, même si le JSON en fournit un
                if (articleInfo.price() == null) {
                    throw new BusinessException("Le prix de l'article est manquant", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                item.setUnitPrice(articleInfo.price());

                // Vérifier le stock
                if (articleInfo.stockQuantity() < item.getQuantity()) {
                    throw new BusinessException(
                        "Stock insuffisant pour l'article " + articleInfo.name(),
                        HttpStatus.BAD_REQUEST
                    );
                }

                // Mettre à jour le stock
                Boolean stockUpdated = externalServiceClient.updateArticleStock(
                    item.getArticleId(),
                    articleInfo.stockQuantity() - item.getQuantity()
                );
                if (stockUpdated == null || !stockUpdated) {
                    throw new BusinessException("Erreur lors de la mise à jour du stock", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

            Order savedOrder = orderRepository.save(order);
            invoiceService.generateInvoice(savedOrder);
            return savedOrder;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("Erreur lors de la création de la commande: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Commande non trouvée", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new BusinessException("Commande non trouvée", HttpStatus.NOT_FOUND));
    }

    public Order updateOrderStatus(Long id, Order.OrderStatus newStatus) {
        try {
            Order order = getOrderById(id);
            
            // Vérifier la transition de statut
            validateStatusTransition(order.getStatus(), newStatus);
            
            order.setStatus(newStatus);
            return orderRepository.save(order);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la mise à jour du statut: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Order updatePaymentStatus(Long id, Order.PaymentStatus newStatus) {
        try {
            Order order = getOrderById(id);
            order.setPaymentStatus(newStatus);
            return orderRepository.save(order);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la mise à jour du statut de paiement: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void cancelOrder(Long id) {
        try {
            Order order = getOrderById(id);

            if (order.getStatus() == Order.OrderStatus.CANCELLED) {
                throw new BusinessException("La commande est déjà annulée", HttpStatus.BAD_REQUEST);
            }

            // Restaurer le stock
            for (OrderItem item : order.getItems()) {
                ExternalServiceClient.ArticleInfo articleInfo = externalServiceClient.getArticleInfo(item.getArticleId());
                if (articleInfo == null) {
                    throw new BusinessException("Article non trouvé", HttpStatus.BAD_REQUEST);
                }
                Boolean stockUpdated = externalServiceClient.updateArticleStock(
                    item.getArticleId(),
                    articleInfo.stockQuantity() + item.getQuantity()
                );
                if (stockUpdated == null || !stockUpdated) {
                    throw new BusinessException("Erreur lors de la restauration du stock", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

            order.setStatus(Order.OrderStatus.CANCELLED);
            order.setPaymentStatus(Order.PaymentStatus.CANCELLED);
            orderRepository.save(order);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de l'annulation de la commande: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatusAndDateRange(
            Order.OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return orderRepository.findByStatusAndOrderDateBetween(status, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByPaymentStatusAndDateRange(
            Order.PaymentStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return orderRepository.findByPaymentStatusAndOrderDateBetween(status, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<OrderWithInvoiceDTO> getAllOrdersWithInvoiceInfo() {
        List<Order> orders = orderRepository.findAll();
        List<OrderWithInvoiceDTO> dtos = new java.util.ArrayList<>();
        for (Order order : orders) {
            var invoiceOpt = invoiceRepository.findByOrder(order);
            boolean hasInvoice = invoiceOpt.isPresent();
            Long invoiceId = hasInvoice ? invoiceOpt.get().getId() : null;
            dtos.add(new OrderWithInvoiceDTO(order, hasInvoice, invoiceId));
        }
        return dtos;
    }

    private String generateOrderNumber() {
        return "CMD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void validateStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        // Implémenter la logique de validation des transitions de statut
        // Par exemple, on ne peut pas passer de CANCELLED à PROCESSING
        if (currentStatus == Order.OrderStatus.CANCELLED && newStatus != Order.OrderStatus.CANCELLED) {
            throw new BusinessException(
                "Impossible de modifier le statut d'une commande annulée",
                HttpStatus.BAD_REQUEST
            );
        }
        
        // Ajouter d'autres règles de validation selon les besoins
    }
} 