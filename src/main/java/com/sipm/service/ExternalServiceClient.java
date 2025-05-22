package com.sipm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ExternalServiceClient {
    private final RestTemplate restTemplate;

    public ArticleInfo getArticleInfo(Long articleId) {
        return restTemplate.getForObject("http://localhost:8081/api/articles/" + articleId, ArticleInfo.class);
    }

    public CustomerInfo getCustomerInfo(Long customerId) {
        return restTemplate.getForObject("http://localhost:8081/api/customers/" + customerId, CustomerInfo.class);
    }

    public Boolean updateArticleStock(Long articleId, Integer quantity) {
        return restTemplate.postForObject("http://localhost:8081/api/articles/" + articleId + "/stock", new StockUpdateRequest(quantity), Boolean.class);
    }

    public record ArticleInfo(
        Long id,
        String code,
        @JsonProperty("nom") String name,
        @JsonProperty("prixUnitaire") BigDecimal price,
        @JsonProperty("quantiteEnStock") Integer stockQuantity
    ) {}

    public record CustomerInfo(
        Long id,
        String name,
        String firstName,
        String email,
        Double creditLimit
    ) {}

    public record StockUpdateRequest(Integer quantity) {}
} 