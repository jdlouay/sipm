package com.sipm.service;

import com.sipm.common.BusinessException;
import com.sipm.model.Article;
import com.sipm.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public Page<Article> getAllArticles(Pageable pageable) {
        return articleRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Article getArticleById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Article non trouvé avec l'id: " + id,
                        "ARTICLE_NOT_FOUND"));
    }

    @Transactional(readOnly = true)
    public Article getArticleByReference(String reference) {
        return articleRepository.findByReference(reference)
                .orElseThrow(() -> new BusinessException(
                        "Article non trouvé avec la référence: " + reference,
                        "ARTICLE_NOT_FOUND"));
    }

    @Transactional
    public Article createArticle(Article article) {
        if (articleRepository.existsByReference(article.getReference())) {
            throw new BusinessException(
                    "Un article avec cette référence existe déjà: " + article.getReference(),
                    "DUPLICATE_REFERENCE");
        }
        return articleRepository.save(article);
    }

    @Transactional
    public Article updateArticle(Long id, Article articleDetails) {
        Article article = getArticleById(id);
        
        // Vérifier si la nouvelle référence n'est pas déjà utilisée par un autre article
        if (!article.getReference().equals(articleDetails.getReference()) &&
            articleRepository.existsByReference(articleDetails.getReference())) {
            throw new BusinessException(
                    "Un article avec cette référence existe déjà: " + articleDetails.getReference(),
                    "DUPLICATE_REFERENCE");
        }

        article.setReference(articleDetails.getReference());
        article.setNom(articleDetails.getNom());
        article.setDescription(articleDetails.getDescription());
        article.setQuantiteEnStock(articleDetails.getQuantiteEnStock());
        article.setPrixUnitaire(articleDetails.getPrixUnitaire());
        article.setCategorie(articleDetails.getCategorie());

        return articleRepository.save(article);
    }

    @Transactional
    public void deleteArticle(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new BusinessException(
                    "Article non trouvé avec l'id: " + id,
                    "ARTICLE_NOT_FOUND");
        }
        articleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<Article> searchArticles(String searchTerm, Pageable pageable) {
        return articleRepository.searchArticles(searchTerm, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Article> findArticlesStockFaible(Integer seuil, Pageable pageable) {
        return articleRepository.findArticlesStockFaible(seuil, pageable);
    }

    @Transactional
    public Article updateStock(Long id, Integer nouvelleQuantite) {
        if (nouvelleQuantite < 0) {
            throw new BusinessException(
                    "La quantité en stock ne peut pas être négative",
                    "INVALID_QUANTITY");
        }

        Article article = getArticleById(id);
        article.setQuantiteEnStock(nouvelleQuantite);
        return articleRepository.save(article);
    }
} 