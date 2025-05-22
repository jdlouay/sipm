package com.sipm.controller;

import com.sipm.model.Article;
import com.sipm.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;

    @GetMapping
    public Page<Article> getAllArticles(Pageable pageable) {
        return articleService.getAllArticles(pageable);
    }

    @GetMapping("/{id}")
    public Article getArticleById(@PathVariable Long id) {
        return articleService.getArticleById(id);
    }

    @PostMapping
    public Article createArticle(@RequestBody Article article) {
        return articleService.createArticle(article);
    }

    @PutMapping("/{id}")
    public Article updateArticle(@PathVariable Long id, @RequestBody Article article) {
        return articleService.updateArticle(id, article);
    }

    @DeleteMapping("/{id}")
    public void deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
    }

    @PutMapping("/{id}/stock")
    public Boolean updateStock(@PathVariable Long id, @RequestBody StockUpdateRequest request) {
        articleService.updateStock(id, request.getQuantity());
        return true;
    }

    @PostMapping("/{id}/stock")
    public Boolean updateStockPost(@PathVariable Long id, @RequestBody StockUpdateRequest request) {
        articleService.updateStock(id, request.getQuantity());
        return true;
    }

    public static class StockUpdateRequest {
        private Integer quantity;
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
} 